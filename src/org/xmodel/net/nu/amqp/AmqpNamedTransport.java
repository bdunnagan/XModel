package org.xmodel.net.nu.amqp;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.AbstractTransport;
import org.xmodel.net.nu.ITransport;

public class AmqpNamedTransport extends AbstractTransport
{
  public AmqpNamedTransport( String publishQueue, String replyQueue, AmqpTransport transport)
  {
    super( transport.getProtocol(), transport.getTransportContext());

    this.publishQueue = publishQueue;
    this.replyQueue = replyQueue;
    this.transport = transport;
  }
  
  @Override
  public void setConnectTimeout( int timeout)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public AsyncFuture<ITransport> connect()
  {
    return null;
  }

  @Override
  public AsyncFuture<ITransport> disconnect()
  {
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
  
  private String publishQueue;
  private String replyQueue;
  private AmqpTransport transport;
  private int references;
}
