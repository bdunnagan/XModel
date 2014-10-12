package org.xmodel.net.nu.amqp;

import java.io.IOException;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.log.SLog;
import org.xmodel.net.nu.AbstractTransport;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.xpath.expression.IContext;

public class AmqpNamedTransport extends AbstractTransport
{
  public AmqpNamedTransport( String publishQueue, String replyQueue, AmqpTransport transport)
  {
    super( transport.getProtocol(), transport.getTransportContext());

    this.publishQueue = publishQueue;
    this.replyQueue = replyQueue;
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
    return transport.publish( "", publishQueue, replyQueue, message, request);
  }

  protected void incrementReferenceCount()
  {
    references++;
  }
  
  protected int decrementReferenceCount()
  {
    return --references;
  }
  
  @Override
  public boolean notifyError( ITransportImpl transport, IContext context, Error error, IModelObject request)
  {
    if ( error == Error.heartbeatLost)
    {
      SLog.errorf( this, "Lost heartbeat on transport, %s", transport);
      this.transport.removeRoute( publishQueue, this);
      return true;
    }

    return super.notifyError( transport, context, error, request);
  }

  private String publishQueue;
  private String replyQueue;
  private AmqpTransport transport;
  private int references;
}
