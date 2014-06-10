package org.xmodel.net.nu;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.xpath.expression.IContext;

public final class RoutedTransport implements ITransport
{
  public RoutedTransport( ITransportImpl via, String at)
  {
    // invoke listeners with this transport
    via.getNotifier().setTransport( this);
    
    this.via = via;
    this.at = at;
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
  public AsyncFuture<ITransport> ack( IModelObject message)
  {
    return via.ack( message);
  }

  @Override
  public AsyncFuture<ITransport> request( IModelObject message, IContext messageContext, int timeout)
  {
    message.setAttribute( "route", at);
    return via.request( message, messageContext, timeout);
  }
  
  @Override
  public AsyncFuture<ITransport> respond( IModelObject message, IModelObject request)
  {
    message.setAttribute( "route", at);
    return via.respond( message, request);
  }
  
  @Override
  public void addListener( IReceiveListener listener)
  {
    via.addListener( listener);
  }
  
  @Override
  public void removeListener( IReceiveListener listener)
  {
    via.removeListener( listener);
  }
  
  @Override
  public void addListener( IConnectListener listener)
  {
    via.addListener( listener);
  }
  
  @Override
  public void removeListener( IConnectListener listener)
  {
    via.removeListener( listener);
  }
  
  @Override
  public void addListener( IDisconnectListener listener)
  {
    via.addListener( listener);
  }
  
  @Override
  public void removeListener( IDisconnectListener listener)
  {
    via.removeListener( listener);
  }

  @Override
  public void addListener( IErrorListener listener)
  {
    via.addListener( listener);
  }
  
  @Override
  public void removeListener( IErrorListener listener)
  {
    via.removeListener( listener);
  }

  public void notifyError( IContext context, ITransport.Error error, IModelObject request)
  {
  }

  private ITransportImpl via;
  private String at;
}
