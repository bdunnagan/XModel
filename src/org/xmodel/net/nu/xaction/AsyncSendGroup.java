package org.xmodel.net.nu.xaction;

import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.IErrorListener;
import org.xmodel.net.nu.IReceiveListener;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xaction.IXAction;
import org.xmodel.xpath.expression.IContext;

public class AsyncSendGroup implements IReceiveListener, IErrorListener
{
  public AsyncSendGroup( String var, IContext callContext)
  {
    this.var = var;
    this.callContext = callContext;
    this.sentCount = new AtomicInteger();
    this.doneCount = new AtomicInteger();
  }

  public void setReceiveScript( IXAction onSuccess)
  {
    this.onReceive = onSuccess;
  }
  
  public void setErrorScript( IXAction onError)
  {
    this.onError = onError;
  }
  
  public void setCompleteScript( IXAction onComplete)
  {
    this.onComplete = onComplete;
  }
  
  public void send( Iterator<ITransport> transports, IModelObject message, IContext messageContext, int timeout)
  {
    int count = 0;
    while( transports.hasNext())
    {
      ITransport transport = transports.next();
      
      addListeners( transport);
      count++;
      
      transport.request( message, messageContext, timeout);
    }

    sentCount.set( count);

    if ( count == doneCount.get())
      notifyComplete();
  }

  public void sendAndWait( Iterator<ITransport> transports, IModelObject message, IContext messageContext, int timeout) throws InterruptedException
  {
    semaphore = new Semaphore( 0);
    send( transports, message, messageContext, timeout);
    semaphore.acquire();
  }
  
  @Override
  public final void onReceive( ITransport transport, IModelObject response, IContext messageContext, IModelObject request)
  {
    removeListeners( transport);
    
    messageContext.getScope().set( var, response);
    
    if ( onReceive != null)
    {
      try
      {
        onReceive.run( messageContext);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
    
    notifyWhenAllRequestsComplete( doneCount.incrementAndGet());
  }
  
  @Override
  public void onError( ITransport transport, IContext messageContext, ITransport.Error error, IModelObject request)
  {
    removeListeners( transport);
    
    messageContext.getScope().set( var, error.toString());
    
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
    transport.addListener( (IErrorListener)this);
  }
  
  private void removeListeners( ITransport transport)
  {
    transport.removeListener( (IReceiveListener)this);
    transport.removeListener( (IErrorListener)this);
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
  private IXAction onReceive;
  private IXAction onError;
  private IXAction onComplete;
  private Semaphore semaphore;
}