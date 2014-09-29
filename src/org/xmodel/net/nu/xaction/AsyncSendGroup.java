package org.xmodel.net.nu.xaction;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.IEventHandler;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.xaction.IXAction;
import org.xmodel.xpath.expression.IContext;

public class AsyncSendGroup
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
      
      transport.getEventPipe().addLast( new EventHandler( transport));
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
  
  public void notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage)
  {
    messageContext.getScope().set( var, message);
    
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

  private void notifyError( IContext context, Error error, IModelObject request)
  {
    context.getScope().set( var, error.toString());
    
    if ( onError != null)
    {
      try
      {
        onError.run( context);
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

  class EventHandler implements IEventHandler
  {
    public EventHandler( ITransport transport)
    {
      this.transport = transport;
    }
    
    @Override
    public boolean notifyConnect(IContext transportContext) throws IOException
    {
      return false;
    }

    @Override
    public boolean notifyDisconnect(IContext transportContext) throws IOException
    {
      return false;
    }

    @Override
    public boolean notifyReceive( ByteBuffer buffer) throws IOException
    {
      return false;
    }

    @Override
    public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage)
    {
      transport.getEventPipe().remove( this);
      AsyncSendGroup.this.notifyReceive( message, messageContext, requestMessage);
      return false;
    }

    @Override
    public boolean notifyError( IContext context, Error error, IModelObject request)
    {
      transport.getEventPipe().remove( this);
      AsyncSendGroup.this.notifyError( context, error, request);
      return false;
    }

    @Override
    public boolean notifyException( IOException e)
    {
      return false;
    }
    
    private ITransport transport;
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