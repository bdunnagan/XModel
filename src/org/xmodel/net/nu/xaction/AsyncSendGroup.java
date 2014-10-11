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
import org.xmodel.net.nu.ITransportImpl;
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
      
      transport.getEventPipe().addLast( new EventHandler());
      count++;

      IEnvelopeProtocol envelopeProtocol = transport.getProtocol().envelope();
      IModelObject envelope = isEnvelope? message: envelopeProtocol.buildRequestEnvelope( null, message, life);
      
      transport.send( envelope, messageContext, timeout, retries, life);
    }

    sentCount.set( count);

    if ( count == doneCount.get())
      notifyComplete();
  }

  public Object[] sendAndWait( Iterator<ITransport> transports, IModelObject message, boolean isEnvelope, IContext messageContext, int timeout, int retries, int life) throws InterruptedException
  {
    semaphore = new Semaphore( 0);
    send( transports, message, isEnvelope, messageContext, timeout, retries, life);
    semaphore.acquire();
    return results;
  }
  
  public void notifyReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject requestMessage)
  {
    // ignore acks
    if ( requestMessage != null && onReceive != null) 
    {
      ModelObject transportNode = new ModelObject( "transport");
      transportNode.setValue( transport);

      // only used by RunAction
      Object[] results = new Object[] { message.getValue()};
      if ( message.getChildren().size() > 0) results[ 0] = message.getChildren();
      // only used by RunAction
      
      synchronized( messageContext)
      {
        this.results = results; // only used by RunAction
        ScriptAction.passVariables( new Object[] { transportNode, message}, messageContext, onReceive);
      }
      
      onReceive.run( messageContext);
    }
    
    notifyWhenAllRequestsComplete( doneCount.incrementAndGet());
  }

  private void notifyError( ITransport transport, IContext context, Error error, IModelObject request)
  {
    if ( onError != null) 
    {
      StatefulContext errorContext = new StatefulContext( context);
      
      ModelObject transportNode = new ModelObject( "transport");
      transportNode.setValue( transport);
      
      ScriptAction.passVariables( new Object[] { transport, error.toString()}, errorContext, onError);
      onError.run( errorContext);
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
    @Override
    public boolean notifyReceive( ITransportImpl transport, IModelObject message, IContext messageContext, IModelObject requestMessage)
    {
      transport.getEventPipe().remove( this);
      AsyncSendGroup.this.notifyReceive( transport, message, messageContext, requestMessage);
      return false;
    }

    @Override
    public boolean notifyError( ITransportImpl transport, IContext context, Error error, IModelObject request)
    {
      transport.getEventPipe().remove( this);
      AsyncSendGroup.this.notifyError( transport, context, error, request);
      return false;
    }
  }
  
  public static Log log = Log.getLog( AsyncSendGroup.class);

  private IContext callContext;
  private AtomicInteger sentCount;
  private AtomicInteger doneCount;
  private IXAction onReceive;
  private IXAction onError;
  private IXAction onComplete;
  private Semaphore semaphore;
  private Object[] results;
}
