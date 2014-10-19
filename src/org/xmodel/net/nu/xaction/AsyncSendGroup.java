package org.xmodel.net.nu.xaction;

import java.util.Iterator;
import java.util.concurrent.SynchronousQueue;
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
  
  public void notifyReceive( ITransport transport, IModelObject envelope, IContext messageContext, IModelObject requestMessage)
  {
    Object[] results = null;
    
    // ignore acks
    if ( requestMessage != null) 
    {
      ModelObject transportNode = new ModelObject( "transport");
      transportNode.setValue( transport);

      // Used by RunAction
      results = new Object[] { transport.getProtocol().envelope().getMessage( envelope)};
      // Used by RunAction
      
      synchronized( messageContext)
      {
        ScriptAction.passVariables( new Object[] { transportNode, envelope}, messageContext, onReceive);
      }
      
      if ( onReceive != null) onReceive.run( messageContext);
    }
    
    notifyWhenAllRequestsComplete( doneCount.incrementAndGet(), results);
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
        syncQueue.offer( (result != null)? result: new Object[ 0]);
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
  private SynchronousQueue<Object[]> syncQueue;
}
