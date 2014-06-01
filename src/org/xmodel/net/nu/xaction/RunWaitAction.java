package org.xmodel.net.nu.xaction;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.future.AsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.net.nu.IRouter;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.Routers;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpression.ResultType;

public class RunWaitAction extends GuardedAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    var = Conventions.getVarName( document.getRoot(), false); 
    atExpr = document.getExpression( "at", true);
    timeoutExpr = document.getExpression( "timeout", true);
    
    if ( timeoutExpr == null) 
    {
      timeoutExpr = document.getExpression( "timeouts", true);
      timeoutEach = true;
    }
    
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    double timeout = timeoutExpr.evaluateNumber( context);
    
    List<ITransport> transports = resolveTransport( context);
    for( ITransport transport: transports)
    {
      if ( timeoutEach)
      {
        Object[] result = runAsync( context, transport, timeout);
        if ( result != null) return result;
      }
      else
      {
        long t0 = System.nanoTime();
        
        Object[] result = runAsync( context, transport, timeout);
        if ( result != null) return result;
        
        timeout -= (System.nanoTime() - t0) / 1e6;
        if ( timeout < 0) timeout = 0;
      }
    }

    
    return null;
  }
  
  private List<ITransport> resolveTransport( IContext context)
  {
    if ( atExpr.getType() == ResultType.NODES)
    {
      Object object = Conventions.getCache( context, atExpr);
      return Collections.singletonList( (ITransport)object).iterator();
    }
    else
    {
      IRouter router = Routers.getDefaultRouter();
      return router.resolve( atExpr.evaluateString( context));
    }
  }
  
  private Object[] runAsync( IContext context, ITransport transport, double timeout, CompletionListener listener)
  {
    IXAction onSuccess = Conventions.getScript( document, context, onSuccessExpr);
    IXAction onError = Conventions.getScript( document, context, onErrorExpr);
    
    try
    {
      AsyncExecution execution = new AsyncExecution( transport, onSuccess, onError, scheduler);
      
      execution.getResponseFuture().addListener( new AsyncFuture.IListener<AsyncExecution>() {
        @Override
        public void notifyComplete( AsyncFuture<AsyncExecution> future) throws Exception
        {
          
        }
      });
      
      execution.send( var, document.getRoot(), transport, (int)timeout); // ignoring write future
      
      return null;
    }
    catch( IOException e)
    {
      return handleRunException( context, onError, e);
    }
  }

  private Object[] handleRunException( IContext context, IXAction onError, IOException e)
  {
    if ( onError != null)
    {
      return onError.run( context);
    }
    else
    {
      log.exception( e);
      return null;
    }
  }
  
  private void timeout( Iterator<ITransport> transports)
  {
  }
  
  private class CompletionListener implements AsyncFuture.IListener<AsyncExecution>
  {
    @Override
    public void notifyComplete( AsyncFuture<AsyncExecution> future) throws Exception
    {
    }
    
  }
  
  public final static Log log = Log.getLog( RunWaitAction.class);
  
  private String var;
  private IExpression atExpr;
  private IExpression timeoutExpr;
  private boolean timeoutEach;
  private IExpression onSuccessExpr;  // each
  private IExpression onErrorExpr;    // each
  private IExpression onCompleteExpr; // all
  private ScheduledExecutorService scheduler;
}
