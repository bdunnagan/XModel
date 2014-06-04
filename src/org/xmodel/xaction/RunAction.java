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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.AsyncFuture.IListener;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;
import org.xmodel.net.IXioCallback;
import org.xmodel.net.IXioClientFactory;
import org.xmodel.net.XioClient;
import org.xmodel.net.XioClientPool;
import org.xmodel.net.XioPeer;
import org.xmodel.net.XioServer;
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
    cancelVar = Xlate.get( document.getRoot(), "cancelVar", (String)null);    
    contextExpr = document.getExpression( "context", true);
    scriptExpr = document.getExpression();
    
    varsExpr = document.getExpression( "vars", true);
    
    hostExpr = document.getExpression( "host", true);
    portExpr = document.getExpression( "port", true);
    serverExpr = document.getExpression( "server", true);
    clientsExpr = document.getExpression( "clients", true);
    
    timeoutExpr = document.getExpression( "timeout", true);
    delayExpr = document.getExpression( "delay", true);
    
    onCompleteExpr = document.getExpression( "onComplete", true);
    onSuccessExpr = document.getExpression( "onSuccess", true);
    onErrorExpr = document.getExpression( "onError", true);
    
    futureVar = Xlate.get( document.getRoot(), "future", (String)null);
    executorExpr = document.getExpression( "executor", true);
    schedulerExpr = document.getExpression( "scheduler", true);
  }

  /**
   * @return Returns true if the execution should be asynchronous.
   */
  private boolean isAsynchronous()
  {
    return onSuccessExpr != null || onErrorExpr != null || onCompleteExpr != null;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override 
  protected Object[] doAction( IContext context)
  {
    if ( serverExpr != null)
    {
      if ( clientsExpr != null)
      {
        String[] clients = getClients( context);
        if ( clients.length > 0)
        {
          runRemote( context, getClients( context));
        }
        else if ( hostExpr != null)
        {
          runRemote( context, getRemoteAddresses( context));
        }
        else
        {
          SLog.warnf( this, "No clients specified.");
        }
      }
      else
      {
        SLog.warnf( this, "Client expression not specified.");
      }
    }
    else if ( hostExpr != null)
    {
      runRemote( context, getRemoteAddresses( context));
    }
    else if ( executorExpr != null)
    {
      runLocalAsync( context);
    }
    else if ( schedulerExpr != null)
    {
      runLocalDelayed( context);
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
      log.severef( "Executor not found, '%s'", executorExpr);
      return;
    }
    
    Executor executor = (Executor)executorNode.getValue();
    IXAction script = getScript( getScriptNode( context));
    
    AsyncFuture<Object[]> future = null;
    if ( futureVar != null)
    {
      future = new AsyncFuture<Object[]>( new Object[ 0]);
      Conventions.putCache( context, futureVar, future);
    }
    
    //
    // Must create a new context here without the original context object, because otherwise the
    // new dispatcher will end up using the original context object's model.
    //
    StatefulContext runContext = new StatefulContext( context.getObject());
    runContext.getScope().copyFrom( context.getScope());
    runContext.setExecutor( executor);
    executor.execute( new ScriptRunnable( runContext, script, future));
  }
  
  /**
   * Execute the script with a specified delay.
   * @param context The context.
   */
  private void runLocalDelayed( IContext context)
  {
    double delay = (delayExpr != null)? delayExpr.evaluateNumber( context): 0;
    
    IModelObject schedulerNode = schedulerExpr.queryFirst( context);
    if ( schedulerNode == null)
    {
      log.warnf( "Scheduler not found, '%s'", schedulerExpr);
      return;
    }
    
    ScheduledExecutorService scheduler = (ScheduledExecutorService)schedulerNode.getValue();
    IXAction script = getScript( getScriptNode( context));
    
    AsyncFuture<Object[]> future = null;
    if ( futureVar != null)
    {
      future = new AsyncFuture<Object[]>( new Object[ 0]);
      Conventions.putCache( context, futureVar, future);
    }
    
    //
    // Must create a new context here without the original context object, because otherwise the
    // new dispatcher will end up using the original context object's model.
    //
    StatefulContext runContext = new StatefulContext( context.getObject());
    runContext.getScope().copyFrom( context.getScope());
    runContext.setExecutor( context.getExecutor());
    scheduler.schedule( new ScriptRunnable( runContext, script, future), (int)delay, TimeUnit.MILLISECONDS);
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
        
        if ( !isAsynchronous())
        {
          if ( !client.isConnected())  // TODO: no longer necessary - remove and test
            client.connect( address, connectionRetries).await( timeout);

          if ( !client.isConnected())
            throw new RuntimeException( "Timeout");
            
          try
          {
            Object[] result = client.execute( (StatefulContext)context, varArray, scriptNode, timeout);
            if ( var != null && result != null && result.length > 0) context.getScope().set( var, result[ 0]);
          }
          finally
          {
            clientPool.release( client);
          }
        }
        else
        {
          final StatefulContext runContext = new StatefulContext( context.getObject());
          runContext.getScope().copyFrom( context.getScope());
          runContext.setExecutor( context.getExecutor());
          runContext.set( "remoteHost", address.getAddress().getHostAddress());
          runContext.set( "remotePort", address.getPort());
          
          final int correlation = correlationCounter.getAndIncrement();
          if ( cancelVar != null)
          {
            IModelObject asyncInvocation = new ModelObject( "asyncInvocation");
            asyncInvocation.setAttribute( "peer", client);
            asyncInvocation.setAttribute( "correlation", correlation);
            context.set( cancelVar, asyncInvocation);
          }
          
          if ( !client.isConnected())
          {
            AsyncFuture<XioClient> future = client.connect( address, connectionRetries);
            future.addListener( new IListener<XioClient>() {
              public void notifyComplete( AsyncFuture<XioClient> future) throws Exception
              {
                if ( future.isSuccess())
                {
                  try
                  {
                    AsyncCallback callback = new AsyncCallback( onComplete, onSuccess, onError);
                    client.execute( runContext, correlation, varArray, scriptNode, callback, timeout);
                  }
                  catch( final Exception e)
                  {
                    context.getExecutor().execute( new Runnable() {
                      public void run()
                      {
                        context.set( "error", e.toString());
                        if ( onError != null) onError.run( context);
                        if ( onComplete != null) onComplete.run( context);
                      }
                    });
                  }
                  finally
                  {
                    if ( client != null) clientPool.release( client);
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
              client.execute( runContext, correlation, varArray, scriptNode, callback, timeout);
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
   * Perform remote execution at the specified clients.
   * @param context The context.
   * @param clients The registered names of the clients.
   */
  private void runRemote( final IContext context, final String[] clients)
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
      XioServer server = (XioServer)Conventions.getCache( context, serverExpr);
      if ( server == null)
      {
        log.warnf( "No server specified in server-initiated remote execution: %s", serverExpr);
        return;
      }
      
      for( String client: clients)
      {
        if ( client == null) continue;
        
        log.debugf( "Remote execution at clients with name, '%s', @name=%s ...", client, Xlate.get( scriptNode, "name", "?"));
        
        Iterator<XioPeer> iterator = server.getPeerRegistry().lookupByName( client);
        if ( !iterator.hasNext()) 
        {
          String error = String.format( "Client '%s' is not registered.", client);
          if ( isAsynchronous())
          {
            StatefulContext runContext = new StatefulContext( context.getObject());
            runContext.getScope().copyFrom( context.getScope());
            runContext.set( "error", error);
            if ( onError != null) onError.run( runContext); else log.warn( error);
            if ( onComplete != null) onComplete.run( runContext);
          }
          else
          {
            log.warnf( error);
            // TODO: need sync error reporting
            //throw new XActionException( error);
          }
        }
        
        while( iterator.hasNext())
        {
          XioPeer peer = iterator.next();

          InetSocketAddress address = peer.getRemoteAddress();
          log.debugf( "Remote execution at named client with address, %s:%d ...", address.getAddress().getHostAddress(), address.getPort());
          
          if ( !isAsynchronous())
          {
            Object[] result = peer.execute( (StatefulContext)context, varArray, scriptNode, timeout);
            if ( var != null && result != null && result.length > 0) context.getScope().set( var, result[ 0]);
          }
          else
          {
            final StatefulContext runContext = new StatefulContext( context.getObject());
            runContext.getScope().copyFrom( context.getScope());
            runContext.setExecutor( context.getExecutor());
            runContext.set( "remoteName", client);
            runContext.set( "remoteHost", address.getAddress().getHostAddress());
            runContext.set( "remotePort", address.getPort());
            
            final int correlation = correlationCounter.getAndIncrement();
            if ( cancelVar != null)
            {
              IModelObject asyncInvocation = new ModelObject( "asyncInvocation");
              asyncInvocation.setAttribute( "peer", peer);
              asyncInvocation.setAttribute( "correlation", correlation);
              context.set( cancelVar, asyncInvocation);
            }
            
            AsyncCallback callback = new AsyncCallback( onComplete, onSuccess, onError);
            peer.execute( runContext, correlation, varArray, scriptNode, callback, timeout);
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
   * Returns the registered names of the clients at which the script will be executed.
   * @param context The context.
   * @return Returns the registered names of the clients.
   */
  private String[] getClients( IContext context)
  {
    if ( clientsExpr.getType( context) == ResultType.NODES)
    {
      List<IModelObject> nodes = clientsExpr.evaluateNodes( context);
      List<String> clients = new ArrayList<String>( nodes.size());
      for( IModelObject node: nodes)
      {
        String client = Xlate.get( node, (String)null);
        if ( client.length() > 0) clients.add( client);
      }
      return clients.toArray( new String[ 0]);
    }
    else
    {
      String name = clientsExpr.evaluateString( context);
      if ( name.length() > 0) return new String[] { name};
      return new String[ 0];
    }
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
    if ( isAsynchronous())
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
    public ScriptRunnable( IContext context, IXAction script, AsyncFuture<Object[]> future)
    {
      this.context = context;
      this.script = script;
      this.future = future;
    }
    
    @Override
    public void run()
    {
      if ( future != null)
      {
        try
        {
          Object[] result = script.run( context);
          if ( future != null)
          {
            future.setInitiator( result);
            future.notifySuccess();
          }
        }
        catch( Exception e)
        {
          if ( future != null) future.notifyFailure( e);
        }
      }
      else
      {
        script.run( context);
      }
    }

    private IContext context;
    private IXAction script;
    private AsyncFuture<Object[]> future;
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
        if ( var != null && results != null && results.length > 0) context.getScope().set( var, results[ 0]);
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
  
  protected class AsyncInvocation
  {
    public AsyncInvocation( XioClient client, int correlation)
    {
      this.client = client;
      this.correlation = correlation;
    }
    
    public XioClient client;
    public int correlation;
  }
  
  private final static Log log = Log.getLog( RunAction.class);
  private final static int[] connectionRetries = { 500, 1000, 3000, 5000};  
  private final static ConcurrentHashMap<Executor, XioClientPool> clientPools = new ConcurrentHashMap<Executor, XioClientPool>();
  private final static AtomicInteger correlationCounter = new AtomicInteger( 0);
  
  private String var;
  private String cancelVar;
  private IExpression varsExpr;
  private IExpression contextExpr;
  private IExpression hostExpr;
  private IExpression portExpr;
  private IExpression serverExpr;
  private IExpression clientsExpr;
  private IExpression timeoutExpr;
  private IExpression delayExpr;
  private IExpression scriptExpr;
  private IModelObject inline;
  private IExpression onCompleteExpr;
  private IExpression onSuccessExpr;
  private IExpression onErrorExpr;
  private String futureVar;
  private IExpression executorExpr;
  private IExpression schedulerExpr;
}
