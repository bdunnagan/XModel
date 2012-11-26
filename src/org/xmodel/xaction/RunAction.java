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

import java.io.IOException;
import java.util.List;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.xmodel.INode;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;
import org.xmodel.net.IXioCallback;
import org.xmodel.net.XioClient;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
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
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override 
  protected Object[] doAction( IContext context)
  {
    if ( hostExpr == null) runLocal( context); else runRemote( context);   
    return null;
  }

  /**
   * Perform local execution.
   * @param context The context.
   * @return Returns the execution result.
   */
  @SuppressWarnings("unchecked")
  private Object[] runLocal( IContext context)
  {
    Object[] results = null;

    IXAction script = getScript( context, scriptExpr);
    if ( script == null) return null;
    
    if ( contextExpr != null)
    {
      for( INode localNode: contextExpr.query( context, null))
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
      if ( result instanceof List) scope.set( var, (List<INode>)result);
      else if ( result instanceof String) scope.set( var, result.toString());
      else if ( result instanceof Number) scope.set( var, (Number)result);
      else if ( result instanceof Boolean) scope.set( var, (Boolean)result);
    }
    
    return null;
  }
  
  /**
   * Perform remote execution.
   * @param context The context.
   * @return Returns the execution result.
   */
  private Object[] runRemote( IContext context)
  {
    String host = (hostExpr != null)? hostExpr.evaluateString( context): null;
    int port = (portExpr != null)? (int)portExpr.evaluateNumber( context): -1;
    int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;

    IXAction onComplete = (onCompleteExpr != null)? getScript( context, onCompleteExpr): null;
    IXAction onSuccess = (onSuccessExpr != null)? getScript( context, onSuccessExpr): null;
    IXAction onError = (onErrorExpr != null)? getScript( context, onErrorExpr): null;
    
    String vars = (varsExpr != null)? varsExpr.evaluateString( context): "";
    String[] varArray = vars.split( "\\s*,\\s*");
    INode scriptNode = getScriptNode( context);

    XioClient client = null;
    try
    {
      log.debugf( "Remote execution at %s:%d, @name=%s ...", host, port, Xlate.get( scriptNode, "name", "?"));
      
      client = new XioClient( context, context);
      
      // execute synchronously unless one of the async callback scripts exists
      if ( onComplete == null && onSuccess == null && onError == null)
      {
        try
        {
          if ( !client.connect( host, port, connectionRetries).await( timeout)) throw new IOException( "Connection not established.");
          Object[] result = client.execute( (StatefulContext)context, varArray, scriptNode, timeout);
          if ( var != null && result != null && result.length > 0) context.getScope().set( var, result[ 0]);
        }
        finally
        {
          if ( client != null) try { client.close();} catch( Exception e) {}
        }
      }
      else
      {
        AsyncExecuter callback = new AsyncExecuter( client, context, varArray, scriptNode, timeout, onComplete, onSuccess, onError);
        client.connect( host, port, connectionRetries).addListener( callback); 
      }
      
      log.debug( "Finished remote.");
    }
    catch( Exception e)
    {
      handleException( e, context, onComplete, onError);
    }
    
    return null;
  }
  
  /**
   * Get the script node to be executed.
   * @param context The context.
   * @return Returns null or the script node.
   */
  private INode getScriptNode( IContext context)
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
      INode scriptNode = expression.queryFirst( context);
      CompiledAttribute attribute = (scriptNode != null)? (CompiledAttribute)scriptNode.getAttribute( "compiled"): null;
      if ( attribute != null) script = attribute.script;
      if ( script == null)
      {
        script = document.createScript( scriptNode);
        if ( script != null)
        {
          scriptNode.setAttribute( "compiled", new CompiledAttribute( script));
        }
        else
        {
          SLog.warnf( this, "Script not found: %s", expression);
        }
      }
    }
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
  
  private final class AsyncExecuter implements IXioCallback, Runnable, ChannelFutureListener
  {
    public AsyncExecuter( XioClient client, IContext context, String[] vars, INode element, int timeout, IXAction onComplete, IXAction onSuccess, IXAction onError)
    {
      this.client = client;
      this.context = context;
      this.vars = vars;
      this.element = element;
      this.timeout = timeout;
      this.onComplete = onComplete;
      this.onSuccess = onSuccess;
      this.onError = onError;
    }
    
    /* (non-Javadoc)
     * @see org.jboss.netty.channel.ChannelFutureListener#operationComplete(org.jboss.netty.channel.ChannelFuture)
     */
    @Override
    public void operationComplete( ChannelFuture future) throws Exception
    {
      this.future = future;
      context.getModel().dispatch( this);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
      if ( !future.isSuccess())
      {
        handleException( future.getCause(), context, onComplete, onError);
      }
      else
      {
        try
        {
          client.execute( context, vars, element, this, timeout);
        }
        catch( Exception e)
        {
          handleException( e, context, onComplete, onError);
        }
      }
    }
    
    /* (non-Javadoc)
     * @see org.xmodel.net.ICallback#onComplete(org.xmodel.xpath.expression.IContext)
     */
    @Override
    public void onComplete( IContext context)
    {
      if ( onComplete != null) onComplete.run( context);
      if ( client != null) try { client.close();} catch( Exception e) {}
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
    
    private XioClient client;
    private IContext context;
    private String[] vars;
    private INode element;
    private int timeout;
    private ChannelFuture future;
    private IXAction onComplete;
    private IXAction onSuccess;
    private IXAction onError;
  }
  
  private final static Log log = Log.getLog( RunAction.class);
  private final static int[] connectionRetries = { 250, 500, 1000, 2000, 3000, 5000};
  
  private String var;
  private IExpression varsExpr;
  private IExpression contextExpr;
  private IExpression hostExpr;
  private IExpression portExpr;
  private IExpression timeoutExpr;
  private IExpression scriptExpr;
  private INode inline;
  private IExpression onCompleteExpr;
  private IExpression onSuccessExpr;
  private IExpression onErrorExpr;
}
