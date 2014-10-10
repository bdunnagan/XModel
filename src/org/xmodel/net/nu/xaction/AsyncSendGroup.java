package org.xmodel.net.nu.xaction;

import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.DefaultEventHandler;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

public class AsyncSendGroup
{
  public AsyncSendGroup( IContext callContext)
  {
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
  
  public void send( Iterator<ITransport> transports, IModelObject message, boolean isEnvelope, IContext messageContext, int timeout, int retries, int life)
  {
    int count = 0;
    while( transports.hasNext())
    {
      ITransport transport = transports.next();
      
      transport.getEventPipe().addLast( new EventHandler( transport));
      count++;

      IEnvelopeProtocol envelopeProtocol = transport.getProtocol().envelope();
      IModelObject envelope = isEnvelope? message: envelopeProtocol.
      
      transport.send( message, messageContext, timeout, retries, life);
    }

    sentCount.set( count);

    if ( count == doneCount.get())
      notifyComplete();
  }

  public void sendAndWait( Iterator<ITransport> transports, IModelObject message, boolean isEnvelope, IContext messageContext, int timeout, int retries, int life) throws InterruptedException
  {
    semaphore = new Semaphore( 0);
    send( transports, message, messageContext, timeout, retries, life);
    semaphore.acquire();
  }
  
  public void notifyReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject requestMessage)
  {
    // ignore acks
    if ( requestMessage != null && onReceive != null) 
    {
      ModelObject transportNode = new ModelObject( "transport");
      transportNode.setValue( transport);
      
      ScriptAction.passVariables( new Object[] { transportNode, message}, messageContext, onReceive);
      onReceive.run( messageContext);
    }
    
    notifyWhenAllRequestsComplete( doneCount.incrementAndGet());
  }

  private void notifyError( ITransport transport, IContext context, Error error, IModelObject request)
  {
    if ( onError != null) 
    {
      StatefulContext messageContext = new StatefulContext( context);
      
      ModelObject transportNode = new ModelObject( "transport");
      transportNode.setValue( transport);
      
      ScriptAction.passVariables( new Object[] { transport, error.toString()}, messageContext, onError);
      onError.run( messageContext);
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

  class EventHandler extends DefaultEventHandler
  {
    public EventHandler( ITransport transport)
    {
      this.transport = transport;
    }
    
    @Override
    public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage)
    {
      transport.getEventPipe().remove( this);
      AsyncSendGroup.this.notifyReceive( transport, message, messageContext, requestMessage);
      return false;
    }

    @Override
    public boolean notifyError( IContext context, Error error, IModelObject request)
    {
      transport.getEventPipe().remove( this);
      AsyncSendGroup.this.notifyError( transport, context, error, request);
      return false;
    }

    private ITransport transport;
  }
  
  public static Log log = Log.getLog( AsyncSendGroup.class);

  private IContext callContext;
  private AtomicInteger sentCount;
  private AtomicInteger doneCount;
  private IXAction onReceive;
  private IXAction onError;
  private IXAction onComplete;
  private Semaphore semaphore;
}
