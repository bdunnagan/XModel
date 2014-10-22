package org.xmodel.net.nu.xaction;

import java.util.Iterator;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.DefaultEventHandler;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol.Type;
import org.xmodel.xpath.expression.IContext;

public abstract class AsyncSendGroup
{
  public AsyncSendGroup( IContext callContext)
  {
    this.callContext = callContext;
    this.sentCount = new AtomicInteger();
    this.doneCount = new AtomicInteger();
  }
  
  protected abstract void onSuccess( ITransport transport, IContext messageContext, IModelObject message, IModelObject request);
  
  protected abstract void onError( ITransport transport, IContext messageContext, Error error, IModelObject request);

  protected abstract void onComplete( IContext callContext);
    
  public void send( Iterator<ITransport> transports, IModelObject message, boolean isEnvelope, IContext messageContext, int timeout, int retries, int life)
  {
    int count = 0;
    while( transports.hasNext())
    {
      ITransport transport = transports.next();
      
      IEnvelopeProtocol envelopeProtocol = transport.getProtocol().envelope();
      IModelObject envelope = isEnvelope? message: envelopeProtocol.buildRequestEnvelope( null, message, life);
      
      transport.getEventPipe().addLast( new EventHandler( envelope));
      count++;

      transport.send( null, envelope, messageContext, timeout, retries, life);
    }

    sentCount.set( count);

    if ( count == doneCount.get())
      notifyComplete();
  }

  public Object[] sendAndWait( Iterator<ITransport> transports, IModelObject message, boolean isEnvelope, IContext messageContext, int timeout, int retries, int life) throws InterruptedException
  {
    syncQueue = new SynchronousQueue<Object[]>();
    send( transports, message, isEnvelope, messageContext, timeout, retries, life);
    return syncQueue.take();
  }
  
  public void notifyReceive( final ITransport transport, final IModelObject envelope, final IContext messageContext, final IModelObject requestMessage)
  {
    // ignore acks
    IEnvelopeProtocol envelopeProtocol = transport.getProtocol().envelope();
    if ( requestMessage != null && envelopeProtocol.getType( envelope) != Type.ack)
    {
      messageContext.getExecutor().execute( new Runnable() {
        public void run()
        {
          onSuccess( transport, messageContext, envelope, requestMessage);
        }
      });
    }
    
    Object[] results = new Object[] { envelopeProtocol.getMessage( envelope)};
    notifyWhenAllRequestsComplete( doneCount.incrementAndGet(), results);
  }
  
  private void notifyError( final ITransport transport, final IContext context, final Error error, final IModelObject request)
  {
    context.getExecutor().execute( new Runnable() {
      public void run()
      {
        onError( transport, context, error, request);
      }
    });
    
    notifyWhenAllRequestsComplete( doneCount.incrementAndGet(), null);
  }

  private void notifyWhenAllRequestsComplete( int requestsCompleted, Object[] result)
  {
    int sent = sentCount.get();
    if ( sent > 0)
    {
      if ( requestsCompleted == sent)
      {
        notifyComplete();
        if ( syncQueue != null) syncQueue.offer( (result != null)? result: new Object[ 0]);
      }
    }
  }

  private void notifyComplete()
  {
    callContext.getExecutor().execute( new Runnable() {
      public void run() 
      {
        try
        {
          onComplete( callContext);
        }
        catch( Exception e)
        {
          log.exception( e);
        }
      }
    });
  }

  class EventHandler extends DefaultEventHandler
  {
    public EventHandler( IModelObject request)
    {
      this.request = request;
    }
    
    @Override
    public boolean notifyReceive( ITransportImpl transport, IModelObject message, IContext messageContext, IModelObject request)
    {
      if ( request == this.request)
      {
        transport.getEventPipe().remove( this);
        AsyncSendGroup.this.notifyReceive( transport, message, messageContext, request);
      }
      return false;
    }

    @Override
    public boolean notifyError( ITransportImpl transport, IContext context, Error error, IModelObject request)
    {
      if ( request == this.request)
      {
        transport.getEventPipe().remove( this);
        AsyncSendGroup.this.notifyError( transport, context, error, request);
      }
      return false;
    }
    
    private IModelObject request;
  }
  
  public static Log log = Log.getLog( AsyncSendGroup.class);

  private IContext callContext;
  private AtomicInteger sentCount;
  private AtomicInteger doneCount;
  private SynchronousQueue<Object[]> syncQueue;
}
