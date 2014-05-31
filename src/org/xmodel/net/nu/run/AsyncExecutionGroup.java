package org.xmodel.net.nu.run;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.IReceiveListener;
import org.xmodel.net.nu.ITimeoutListener;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xaction.IXAction;
import org.xmodel.xpath.expression.IContext;

public class AsyncExecutionGroup implements IReceiveListener, ITimeoutListener
{
  public static final String timeoutMessage = "Timeout";
  public static final String cancelledMessage = "Cancelled";
  
  public AsyncExecutionGroup( String var, IContext callContext, Iterator<ITransport> transports)
  {
    this.var = var;
    this.callContext = callContext;
    this.sentCount = new AtomicInteger();
    this.doneCount = new AtomicInteger();
  }

  public void setSuccessScript( IXAction onSuccess)
  {
    this.onSuccess = onSuccess;
  }
  
  public void setErrorScript( IXAction onError)
  {
    this.onError = onError;
  }
  
  public void setCompleteScript( IXAction onComplete)
  {
    this.onComplete = onComplete;
  }
  
  public void send( IModelObject script, IContext messageContext, int timeout) throws IOException
  {
    int count = 0;
    while( transports.hasNext())
    {
      ITransport transport = transports.next();
      addListeners( transport);
      transport.send( script, messageContext, timeout);
      count++;
    }

    //
    // REVISIT THIS EXCLUSION
    //
    sentCount.set( count);

    if ( count == doneCount.get())
      notifyComplete();
  }
  
  @Override
  public final void onReceive( ITransport transport, IModelObject response, IContext messageContext, IModelObject request)
  {
    removeListeners( transport);
    
    if ( onSuccess != null)
    {
      try
      {
        messageContext.getScope().set( var, response);
        onSuccess.run( messageContext);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
    
    notifyWhenAllRequestsComplete( doneCount.incrementAndGet());
  }
  
  @Override
  public void onTimeout( ITransport transport, IModelObject message, IContext messageContext)
  {
    removeListeners( transport);
    
    if ( onError != null)
    {
      try
      {
        messageContext.getScope().set( var, timeoutMessage);
        onError.run( messageContext);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
    
    notifyWhenAllRequestsComplete( doneCount.incrementAndGet());
  }
  
  private void notifyWhenAllRequestsComplete( int requestsCompleted)
  {
    int sent = sentCount.get();
    if ( sent > 0)
    {
      if ( requestsCompleted == sent)
        notifyComplete();
    }
  }

  private void addListeners( ITransport transport)
  {
    transport.addListener( (IReceiveListener)this);
    transport.addListener( (ITimeoutListener)this);
  }
  
  private void removeListeners( ITransport transport)
  {
    transport.removeListener( (IReceiveListener)this);
    transport.removeListener( (ITimeoutListener)this);
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

  private String var;
  private IContext callContext;
  private Iterator<ITransport> transports;
  private AtomicInteger sentCount;
  private AtomicInteger doneCount;
  private IXAction onSuccess;
  private IXAction onError;
  private IXAction onComplete;
}
