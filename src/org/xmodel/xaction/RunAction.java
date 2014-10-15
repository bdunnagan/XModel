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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.future.AsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.xaction.ActionUtil;
import org.xmodel.net.nu.xaction.AsyncSendGroup;
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
    
    viaExpr = document.getExpression( "via", true);
    if ( viaExpr == null) viaExpr = document.getExpression( "registry", true);
    toExpr = document.getExpression( "to", true);
    if ( toExpr == null) toExpr = document.getExpression( "clients", true);
    
    timeoutExpr = document.getExpression( "timeout", true);
    retriesExpr = document.getExpression( "timeout", true);
    lifeExpr = document.getExpression( "timeout", true);
    
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
    if ( viaExpr != null)
    {
      runRemote( context);
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
  private void runLocalSync( IContext context)
  {
    Object[] results = null;

    IXAction script = Conventions.getScript( getDocument(), context, scriptExpr);
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
    
    setVar( context, results);
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
    IXAction script = Conventions.getScript( getDocument(), getScriptNode( context));
    
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
    IXAction script = Conventions.getScript( getDocument(), getScriptNode( context));
    
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
  private void runRemote( IContext context)
  {
    int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
    int life = (lifeExpr != null)? (int)lifeExpr.evaluateNumber( context): -1;
    int retries = (retriesExpr != null)? (int)retriesExpr.evaluateNumber( context): (life >= 0)? 0: -1;
    
    IXAction onSuccess = (onSuccessExpr != null)? Conventions.getScript( getDocument(), context, onSuccessExpr): null;
    IXAction onError = (onErrorExpr != null)? Conventions.getScript( getDocument(), context, onErrorExpr): null;
    IXAction onComplete = (onCompleteExpr != null)? Conventions.getScript( getDocument(), context, onCompleteExpr): null;
    
    IModelObject script = getScriptNode( context);
    transferVariables( script, context);
    
    AsyncSendGroup asyncGroup = new AsyncSendGroup( context);
    asyncGroup.setReceiveScript( onSuccess);
    asyncGroup.setErrorScript( onError);
    asyncGroup.setCompleteScript( onComplete);
    
    Iterator<ITransport> transports = ActionUtil.resolveTransport( context, viaExpr, toExpr);
    if ( isAsynchronous())
    {
      asyncGroup.send( transports, script, false, new StatefulContext( context), timeout, retries, life);
    }
    else
    {
      try
      {
        StatefulContext runContext = new StatefulContext( context);
        Object[] results = asyncGroup.sendAndWait( transports, script, false, runContext, timeout, retries, life);
        
        if ( results != null && results.length > 0) 
        {
          Throwable thrown = getThrowable( results[ 0]);
          if ( thrown != null) handleException( thrown, runContext, onComplete, onError);
        }
        
        setVar( runContext, results);
      }
      catch( InterruptedException e)
      {
        throw new XActionException( e);
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private Throwable getThrowable( Object result)
  {
    if ( result instanceof List)
    {
      List<IModelObject> elements = (List<IModelObject>)result;
      if ( elements.size() > 0)
      {
        IModelObject element = elements.get( 0);
        if ( element.isType( "exception"))
        {
          return (Throwable)element.getValue();
        }
      }
    }
    
    return null;
  }
  
  @SuppressWarnings("unchecked")
  private void setVar( IContext context, Object[] results)
  {
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
  
  @SuppressWarnings("unchecked")
  private void transferVariables( IModelObject script, IContext context)
  {
    int opInsertIndex = 0;
    String vars = (varsExpr != null)? varsExpr.evaluateString( context): "";
    String[] varArray = vars.split( "\\s*,\\s*");
    for( String var: varArray)
    {
      if ( var.length() == 0) continue;
      
      IModelObject varAssignOp = new ModelObject( "assign");
      varAssignOp.setAttribute( "var", var);
      script.addChild( varAssignOp, opInsertIndex++);
      
      Object value = context.get( var);
      if ( value instanceof List)
      {
        for( IModelObject element: (List<IModelObject>)value)
          varAssignOp.addChild( element);
      }
      else
      {
        varAssignOp.setAttribute( "mode", "inject");
        varAssignOp.setValue( value);
      }
    }
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
          if ( future != null) 
          {
            future.notifyFailure( e);
          }
          else
          {
            log.errorf( "Caught error running script, %s...", script);
            log.exception( e);
          }
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
  
  private final static Log log = Log.getLog( RunAction.class);
  
  private String var;
  private IExpression varsExpr;
  private IExpression contextExpr;
  private IExpression viaExpr;
  private IExpression toExpr;
  private IExpression timeoutExpr;
  private IExpression retriesExpr;
  private IExpression lifeExpr;
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
