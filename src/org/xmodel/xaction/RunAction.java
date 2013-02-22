/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * InvokeAction.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
  /*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.log.Log;
import org.xmodel.net.IXioCallback;
import org.xmodel.net.IXioClientFactory;
import org.xmodel.net.XioClient;
import org.xmodel.net.XioClientPool;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpression.ResultType;
import org.xmodel.xpath.expression.StatefulContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * An XAction that executes a script identified by an expression.
 * 
 * TODO: RunAction has several problems:
 *       
 * 1. There is no control over the variables that get returned in the scope of a remote invocation,
 *    which leads to huge execution responses.
 * 
 * 2. There is no support for the basic mechanism of function arguments.
 *    So, while it is easy to assign variables expected by the target of the RunAction,
 *    it is cumbersome to insulate the code surrounding the invocation fromt these variable
 *    assignments.
 */
public class RunAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    var = Conventions.getVarName( document.getRoot(), false, "assign");    
    contextExpr = document.getExpression( "context", true);
    scriptExpr = document.getExpression();
    
    varsExpr = document.getExpression( "vars", true);
    hostExpr = document.getExpression( "host", true);
    portExpr = document.getExpression( "port", true);
    timeoutExpr = document.getExpression( "timeout", true);
    
    onCompleteExpr = document.getExpression( "onComplete", true);
    onSuccessExpr = document.getExpression( "onSuccess", true);
    onErrorExpr = document.getExpression( "onError", true);
    
    executorExpr = document.getExpression( "executor", true);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override 
  protected Object[] doAction( IContext context)
  {
    if ( hostExpr != null)
    {
      runRemote( context, getRemoteAddresses( context));
    }
    else if ( executorExpr != null)
    {
      runLocalAsync( context);
    }
    else
    {
      runLocalSync( context); 
    }
    return null;
  }

  /**
   * Perform local execution.
   * @param context The context.
   */
  @SuppressWarnings("unchecked")
  private void runLocalSync( IContext context)
  {
    Object[] results = null;

    IXAction script = getScript( context, scriptExpr);
    if ( script == null) return;
    
    if ( contextExpr != null)
    {
      for( IModelObject localNode: contextExpr.query( context, null))
      {
        StatefulContext local = new StatefulContext( context, localNode);
        results = script.run( local);
      }
    }
    else
    {
      results = script.run( context);
    }
    
    if ( var != null && results != null && results.length > 0)
    {
      Object result = results[ 0];
      IVariableScope scope = context.getScope();
      if ( result instanceof List) scope.set( var, (List<IModelObject>)result);
      else if ( result instanceof String) scope.set( var, result.toString());
      else if ( result instanceof Number) scope.set( var, (Number)result);
      else if ( result instanceof Boolean) scope.set( var, (Boolean)result);
    }
  }
  
  /**
   * Dispatch the script via the specified dispatcher.
   * @param context The context.
   * @param dispatcher The dispatcher.
   */
  private void runLocalAsync( IContext context)
  {
    IModelObject executorNode = executorExpr.queryFirst( context);
    if ( executorNode == null)
    {
      log.warnf( "Executor not found, '%s'", executorExpr);
      return;
    }
    
    Executor executor = (Executor)executorNode.getValue();
    IXAction script = getScript( getScriptNode( context));
    
    //
    // Must create a new context here without the original context object, because otherwise the
    // new dispatcher will end up using the original context object's model.
    //
    StatefulContext runContext = new StatefulContext( context.getObject());
    runContext.getScope().copyFrom( context.getScope());
    runContext.setExecutor( executor);
    executor.execute( new ScriptRunnable( runContext, script));
  }
  
  /**
   * Perform remote execution.
   * @param context The context.
   */
  private void runRemote( final IContext context, final InetSocketAddress[] addresses)
  {
    final int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;

    final IXAction onComplete = (onCompleteExpr != null)? getScript( context, onCompleteExpr): null;
    final IXAction onSuccess = (onSuccessExpr != null)? getScript( context, onSuccessExpr): null;
    final IXAction onError = (onErrorExpr != null)? getScript( context, onErrorExpr): null;
    
    String vars = (varsExpr != null)? varsExpr.evaluateString( context): "";
    final String[] varArray = vars.split( "\\s*,\\s*");
    final IModelObject scriptNode = getScriptNode( context);

    try
    {
      for( InetSocketAddress address: addresses)
      {
        log.debugf( "Remote execution at %s, @name=%s ...", address.toString(), Xlate.get( scriptNode, "name", "?"));
  
        final XioClientPool clientPool = getClientPool( context);
        final XioClient client = clientPool.lease( address);
        
        if ( onComplete == null && onSuccess == null && onError == null)
        {
          if ( !client.isConnected())
            client.connect( address, connectionRetries).await( timeout);
          
          Object[] result = client.execute( (StatefulContext)context, varArray, scriptNode, timeout);
          if ( var != null && result != null && result.length > 0) context.getScope().set( var, result[ 0]);
        }
        else
        {
          final StatefulContext runContext = new StatefulContext( context.getObject());
          runContext.getScope().copyFrom( context.getScope());
          runContext.setExecutor( context.getExecutor());
          runContext.set( "remoteHost", address.getHostName());
          runContext.set( "remotePort", address.getPort());
          
          if ( !client.isConnected())
          {
            ChannelFuture future = client.connect( address, connectionRetries);
            future.addListener( new ChannelFutureListener() {
              public void operationComplete( ChannelFuture future) throws Exception
              {
                if ( future.isSuccess())
                {
                  try
                  {
                    AsyncCallback callback = new AsyncCallback( onComplete, onSuccess, onError);
                    client.execute( runContext, varArray, scriptNode, callback, timeout);
                  }
                  catch( final Exception e)
                  {
                    if ( client != null) clientPool.release( client);
                    
                    context.getExecutor().execute( new Runnable() {
                      public void run()
                      {
                        context.set( "error", e.toString());
                        if ( onError != null) onError.run( context);
                        if ( onComplete != null) onComplete.run( context);
                      }
                    });
                  }
                }
                else
                {
                  context.getExecutor().execute( new Runnable() {
                    public void run()
                    {
                      context.set( "error", "Connection not established!");
                      if ( onError != null) onError.run( context);
                      if ( onComplete != null) onComplete.run( context);
                    }
                  });
                }
              }
            });
          }
          else
          {
            try
            {
              AsyncCallback callback = new AsyncCallback( onComplete, onSuccess, onError);
              client.execute( runContext, varArray, scriptNode, callback, timeout);
            }
            finally
            {
              if ( client != null) clientPool.release( client);
            }
          }
        }
      }
      
      log.debug( "Finished remote.");
    }
    catch( Exception e)
    {
      handleException( e, context, onComplete, onError);
    }
  }
  
  /**
   * Returns the addresses of the execution hosts.
   * @param context The context.
   * @return Returns the addresses of the execution hosts.
   */
  private InetSocketAddress[] getRemoteAddresses( IContext context)
  {
    String[] hosts = getRemoteHosts( context);
    int[] ports = getRemotePorts( context);
    int count = (hosts.length > ports.length)? hosts.length: ports.length;
    
    InetSocketAddress[] addresses = new InetSocketAddress[ count];
    for( int i=0; i<count; i++)
    {
      if ( i >= hosts.length)
      {
        addresses[ i] = new InetSocketAddress( hosts[ hosts.length - 1], ports[ i]);
      }
      else if ( i >= ports.length)
      {
        addresses[ i] = new InetSocketAddress( hosts[ i], ports[ ports.length - 1]);
      }
      else
      {
        addresses[ i] = new InetSocketAddress( hosts[ i], ports[ i]);
      }
    }
    
    return addresses;
  }
  
  /**
   * Returns the remote hosts.
   * @param context The context.
   * @return Returns the remote hosts.
   */
  private String[] getRemoteHosts( IContext context)
  {
    ResultType resultType = hostExpr.getType( context);
    if ( resultType == ResultType.NODES)
    {
      List<IModelObject> nodes = hostExpr.evaluateNodes( context);
      String[] hosts = new String[ nodes.size()];
      for( int i=0; i<nodes.size(); i++)
        hosts[ i] = Xlate.get( nodes.get( i), "");
      return hosts;
    }
    else
    {
      return new String[] { hostExpr.evaluateString( context)};
    }
  }
  
  /**
   * Returns the remote ports.
   * @param context The context.
   * @return Returns the remote ports.
   */
  private int[] getRemotePorts( IContext context)
  {
    ResultType resultType = portExpr.getType( context);
    if ( resultType == ResultType.NODES)
    {
      List<IModelObject> nodes = portExpr.evaluateNodes( context);
      int[] ports = new int[ nodes.size()];
      for( int i=0; i<nodes.size(); i++)
        ports[ i] = Xlate.get( nodes.get( i), 0);
      return ports;
    }
    else
    {
      return new int[] { (int)portExpr.evaluateNumber( context)};
    }
  }
  
  /**
   * Returns the XioClientPool associated with the specified context.
   * @param context The context.
   * @return Returns the XioClientPool associated with the specified context.
   */
  private XioClientPool getClientPool( final IContext context)
  {
    XioClientPool clientPool = clientPools.get( context.getExecutor());
    if ( clientPool == null)
    {
      clientPool = new XioClientPool( new IXioClientFactory() {
        public XioClient newInstance( InetSocketAddress address)
        {
          return new XioClient( context.getExecutor());
        }
      });
      // TODO: potential memory leak!!
      clientPools.put( context.getExecutor(), clientPool);
    }
    return clientPool;
  }
  
  /**
   * Get the script node to be executed.
   * @param context The context.
   * @return Returns null or the script node.
   */
  private IModelObject getScriptNode( IContext context)
  {
    if ( scriptExpr != null) return scriptExpr.queryFirst( context);
    
    if ( inline == null)
    {
      inline = new ModelObject( "script");
      ModelAlgorithms.copyChildren( document.getRoot(), inline, null);
    }
    return inline;
  }
  
  /**
   * Get the script from the specified expression.
   * @param context The context.
   * @param expression The script expression.
   * @return Returns null or the script.
   */
  private IXAction getScript( IContext context, IExpression expression)
  {
    IXAction script = null;
    if ( expression != null)
    {
      script = getScript( expression.queryFirst( context));
      if ( script == null) log.warnf( "Script not found for expression, %s", expression);
    }
    return script;
  }

  /**
   * Compile, or get the already compiled, script for the specified node.
   * @param scriptNode The script node.
   * @return Returns null or the script.
   */
  private IXAction getScript( IModelObject scriptNode)
  {
    if ( scriptNode == null) return null;
    
    CompiledAttribute attribute = (scriptNode != null)? (CompiledAttribute)scriptNode.getAttribute( "compiled"): null;
    if ( attribute != null) return attribute.script;
    
    IXAction script = document.createScript( scriptNode);
    if ( script != null) scriptNode.setAttribute( "compiled", new CompiledAttribute( script));
    return script;
  }
  
  /**
   * Handle an exception thrown during async execution.
   * @param t The exception.
   * @param context The execution context.
   * @param onComplete The onComplete script.
   * @param onError The onError script.
   */
  private void handleException( Throwable t, IContext context, IXAction onComplete, IXAction onError)
  {
    if ( onComplete != null || onError != null)
    {
      context.set( "error", t.getMessage());
      if ( onError != null) onError.run( context);
      if ( onComplete != null) onComplete.run( context);
    }
    else
    {
      throw new XActionException( t);
    }
  }
  
  // TODO: Move this mechanism to GlobalSettings or IModel
  private final static class CompiledAttribute
  {
    public CompiledAttribute( IXAction script)
    {
      this.script = script;
    }
    
    public IXAction script;
  }
  
  private final static class ScriptRunnable implements Runnable
  {
    public ScriptRunnable( IContext context, IXAction script)
    {
      this.context = context;
      this.script = script;
    }
    
    @Override
    public void run()
    {
      script.run( context);
    }
    
    private IContext context;
    private IXAction script;
  }
  
  private final class AsyncCallback implements IXioCallback
  {
    public AsyncCallback( IXAction onComplete, IXAction onSuccess, IXAction onError)
    {
      this.onComplete = onComplete;
      this.onSuccess = onSuccess;
      this.onError = onError;
    }
    
    /* (non-Javadoc)
     * @see org.xmodel.net.ICallback#onComplete(org.xmodel.xpath.expression.IContext)
     */
    @Override
    public void onComplete( IContext context)
    {
      if ( onComplete != null) onComplete.run( context);
    }

    /* (non-Javadoc)
     * @see org.xmodel.net.ICallback#onSuccess(org.xmodel.xpath.expression.IContext, java.lang.Object[])
     */
    @Override
    public void onSuccess( IContext context, Object[] results)
    {
      if ( onSuccess != null) 
      {
        if ( var != null && results.length > 0) context.getScope().set( var, results[ 0]);
        onSuccess.run( context);
      }
    }

    /* (non-Javadoc)
     * @see org.xmodel.net.ICallback#onError(org.xmodel.xpath.expression.IContext)
     */
    @Override
    public void onError( IContext context, String error)
    {
      if ( onError != null) 
      {
        context.set( "error", error);
        onError.run( context);
      }
    }
    
    private IXAction onComplete;
    private IXAction onSuccess;
    private IXAction onError;
  }
  
  private final static Log log = Log.getLog( RunAction.class);
  private final static int[] connectionRetries = { 250, 500, 1000, 2000, 3000, 5000};  
  private final static ConcurrentHashMap<Executor, XioClientPool> clientPools = new ConcurrentHashMap<Executor, XioClientPool>();
  
  private String var;
  private IExpression varsExpr;
  private IExpression contextExpr;
  private IExpression hostExpr;
  private IExpression portExpr;
  private IExpression timeoutExpr;
  private IExpression scriptExpr;
  private IModelObject inline;
  private IExpression onCompleteExpr;
  private IExpression onSuccessExpr;
  private IExpression onErrorExpr;
  private IExpression executorExpr;
}
