package org.xmodel.net.nu.run;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xaction.IXAction;
import org.xmodel.xpath.expression.IContext;

public class AsyncExecutionGroup
{
  public enum TimeoutMode { all, each};
  
  public AsyncExecutionGroup( IContext callContext, Iterator<ITransport> transports, IXAction onSuccess, IXAction onError, IXAction onComplete)
  {
    this.callContext = callContext;
    
    this.onSuccess = onSuccess;
    this.onError = onError;
    this.onComplete = onComplete;
    
    this.pendingCount = new AtomicInteger();
    this.completeCount = new AtomicInteger();
  }
  
  public void setTimeoutMode( TimeoutMode mode)
  {
    this.mode = mode;
  }
  
  public void send( IModelObject script, IContext messageContext, int timeout) throws IOException
  {
    int count = 0;
    while( transports.hasNext())
    {
      ITransport transport = transports.next();
      count++;
      
      if ( mode == TimeoutMode.each)
      {
        transport.send( script, messageContext, timeout);
      }
      else
      {
        long t0 = System.nanoTime();

        transport.send( script, messageContext, timeout);
        
        timeout -= (System.nanoTime() - t0) / 1e6;
        if ( timeout < 0) timeout = 0;
      }
    }

    //
    // REVISIT THIS EXCLUSION
    //
    pendingCount.set( count);

    if ( count == completeCount.get())
      notifyComplete();
  }
  
  protected void notifySuccess( AsyncExecution execution, IContext context, IModelObject response)
  {
    if ( onSuccess != null)
    {
      try
      {
        context.getScope().set( var, response);
        onSuccess.run( context);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }

    if ( pendingCount.get() > 0)
    {
      if ( completeCount.incrementAndGet() == pendingCount.get())
        notifyComplete();
    }
  }
  
  protected void notifyError( AsyncExecution execution, IContext context, Object error)
  {
    if ( onError != null)
    {
      try
      {
        context.getScope().set( var, error);
        onError.run( context);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
    
    if ( pendingCount.get() > 0)
    {
      if ( completeCount.incrementAndGet() == pendingCount.get())
        notifyComplete();
    }
  }
  
  private void notifyComplete()
  {
    IXAction onComplete;
    
    synchronized( this)
    {
      onComplete = this.onComplete;
      this.onComplete = null;
    }
    
    if ( onComplete != null)
    {
      try
      {
        onComplete.run( callContext);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }

  public static Log log = Log.getLog( AsyncExecutionGroup.class);

  private TimeoutMode mode;
  private String var;
  private IContext callContext;
  private Iterator<ITransport> transports;
  private AtomicInteger pendingCount;
  private AtomicInteger completeCount;
  private IXAction onSuccess;
  private IXAction onError;
  private IXAction onComplete;
}
