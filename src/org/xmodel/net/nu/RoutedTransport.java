package org.xmodel.net.nu;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.xpath.expression.IContext;

public final class RoutedTransport implements ITransport
{
  public RoutedTransport( ITransportImpl via, String to)
  {
    this.via = via;
    this.to = to;
  }

  @Override
  public AsyncFuture<ITransport> connect( int timeout)
  {
    return via.connect( timeout);
  }
  
  @Override
  public AsyncFuture<ITransport> disconnect()
  {
    return via.disconnect();
  }
  
  @Override
  public AsyncFuture<ITransport> register( String name, IContext messageContext, int timeout, int retries)
  {
    // TODO: Is this correct???
    return via.register( name, messageContext, timeout, retries);
  }

  @Override
  public AsyncFuture<ITransport> deregister( String name, IContext messageContext, int timeout, int retries)
  {
    // TODO: Is this correct???
    return via.deregister( name, messageContext, timeout, retries);
  }

  @Override
  public AsyncFuture<ITransport> ack( IModelObject message)
  {
    return via.ack( message);
  }

  @Override
  public AsyncFuture<ITransport> request( IModelObject message, IContext messageContext, int timeout, int retries)
  {
    message.setAttribute( "route", to);
    return via.request( message, messageContext, timeout, retries);
  }
  
  @Override
  public AsyncFuture<ITransport> respond( IModelObject message, IModelObject request)
  {
    message.setAttribute( "route", to);
    return via.respond( message, request);
  }
  
  @Override
  public EventPipe getEventPipe()
  {
    return via.getEventPipe();
  }

  private ITransportImpl via;
  private String to;
}
