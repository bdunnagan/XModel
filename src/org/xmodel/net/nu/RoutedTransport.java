package org.xmodel.net.nu;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

public final class RoutedTransport implements ITransport
{
  public RoutedTransport( ITransportImpl via, String to)
  {
    this.via = via;
    this.to = to;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.ITransport#setConnectTimeout(int)
   */
  @Override
  public void setConnectTimeout( int timeout)
  {
    via.setConnectTimeout( timeout);
  }

  @Override
  public AsyncFuture<ITransport> connect()
  {
    return via.connect();
  }
  
  @Override
  public AsyncFuture<ITransport> disconnect(boolean reconnect)
  {
    return via.disconnect(false);
  }
  
  @Override
  public AsyncFuture<ITransport> send( IModelObject request, IModelObject message, IContext messageContext, int timeout, int retries, int life)
  {
    return via.send( request, message, messageContext, timeout, retries, life);
  }
  
  @Override
  public AsyncFuture<ITransport> sendAck( IModelObject envelope)
  {
    return via.sendAck( envelope);
  }

  @Override
  public EventPipe getEventPipe()
  {
    return via.getEventPipe();
  }

  @Override
  public Protocol getProtocol()
  {
    return via.getProtocol();
  }

  private ITransportImpl via;
  private String to;
}
