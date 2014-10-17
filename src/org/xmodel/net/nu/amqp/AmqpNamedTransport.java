package org.xmodel.net.nu.amqp;

import java.io.IOException;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.FailureAsyncFuture;
import org.xmodel.net.nu.EventPipe;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

public class AmqpNamedTransport implements ITransportImpl
{
  public AmqpNamedTransport( String publishQueue, AmqpTransport transport)
  {
    this.publishQueue = publishQueue;
    this.transport = transport;
  }

  public String getPublishQueue()
  {
    return publishQueue;
  }
  
  @Override
  public void setConnectTimeout( int timeout)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public AsyncFuture<ITransport> connect()
  {
    try
    {
      getEventPipe().notifyConnect( this, getTransportContext());
    }
    catch( IOException e)
    {
      getEventPipe().notifyException( this, e);
    }
    return null;
  }

  @Override
  public AsyncFuture<ITransport> disconnect()
  {
    try
    {
      getEventPipe().notifyDisconnect( this, getTransportContext());
    }
    catch( IOException e)
    {
      getEventPipe().notifyException( this, e);
    }
    return null;
  }

  @Override
  public AsyncFuture<ITransport> sendImpl( IModelObject message, IModelObject request)
  {
    return transport.publish( "", publishQueue, message, request);
  }

  @Override
  public AsyncFuture<ITransport> send( IModelObject request, IModelObject envelope, IContext messageContext, int timeout, int retries, int life)
  {
    try
    {
      getEventPipe().notifySend( this, envelope, messageContext, timeout, retries, life);
      return sendImpl( envelope, null);
    }
    catch( IOException e)
    {
      AmqpTransport.log.exception( e);
      return new FailureAsyncFuture<ITransport>( this, e);
    }
  }

  @Override
  public AsyncFuture<ITransport> sendAck( IModelObject envelope)
  {
    return sendImpl( transport.getProtocol().envelope().buildAck( envelope), envelope);
  }

  @Override
  public EventPipe getEventPipe()
  {
    return transport.getEventPipe();
  }

  @Override
  public Protocol getProtocol()
  {
    return transport.getProtocol();
  }

  @Override
  public IContext getTransportContext()
  {
    return transport.getTransportContext();
  }
  
  @Override
  public int hashCode()
  {
    return transport.hashCode();
  }

  @Override
  public boolean equals( Object object)
  {
    return object == this || transport.equals( object);
  }

  private String publishQueue;
  private AmqpTransport transport;
}
