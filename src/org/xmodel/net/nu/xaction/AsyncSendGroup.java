package org.xmodel.net.nu.xaction;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.IReceiveListener;
import org.xmodel.net.nu.ITimeoutListener;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xaction.IXAction;
import org.xmodel.xpath.expression.IContext;

public class AsyncSendGroup implements IReceiveListener, ITimeoutListener
{
  public static final String timeoutMessage = "Timeout";
  public static final String cancelledMessage = "Cancelled";
  
  public AsyncSendGroup( String var, IContext callContext)
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
  
  public void send( Iterator<ITransport> transports, IModelObject script, IContext messageContext, int timeout)
  {
    int count = 0;
    while( transports.hasNext())
    {
      ITransport transport = transports.next();
      
      addListeners( transport);
      count++;
      
      try
      {
        transport.send( script, messageContext, timeout);
      }
      catch( IOException e)
      {
        notifyError( e);
      }
    }

    sentCount.set( count);

    if ( count == doneCount.get())
      notifyComplete();
  }

  public void sendAndWait( Iterator<ITransport> transports, IModelObject script, IContext messageContext, int timeout) throws InterruptedException
  {
    semaphore = new Semaphore( 0);
    send( transports, script, messageContext, timeout);
    semaphore.acquire();
  }
  
  @Override
  public final void onReceive( ITransport transport, IModelObject response, IContext messageContext, IModelObject request)
  {
    removeListeners( transport);
    
    messageContext.getScope().set( var, response);
    
    if ( onSuccess != null)
    {
      try
      {
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
    
    messageContext.getScope().set( var, timeoutMessage);
    
    if ( onError != null)
    {
      try
      {
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

  private void notifyError( Throwable t)
  {
    if ( onError != null)
    {
      try
      {
        onError.run( callContext);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
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
    
    if ( semaphore != null) semaphore.release();
  }

  public static Log log = Log.getLog( AsyncSendGroup.class);

  private String var;
  private IContext callContext;
  private AtomicInteger sentCount;
  private AtomicInteger doneCount;
  private IXAction onSuccess;
  private IXAction onError;
  private IXAction onComplete;
  private Semaphore semaphore;
}
