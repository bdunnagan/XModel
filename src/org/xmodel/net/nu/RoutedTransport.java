package org.xmodel.net.nu;

import java.io.IOException;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.xpath.expression.IContext;

public final class RoutedTransport implements ITransport
{
  public RoutedTransport( ITransport via, String at)
  {
    this.via = via;
    this.at = at;
  }

  @Override
  public AsyncFuture<ITransport> connect( int timeout) throws IOException
  {
    return via.connect( timeout);
  }
  
  @Override
  public AsyncFuture<ITransport> disconnect() throws IOException
  {
    return via.disconnect();
  }
  
  @Override
  public AsyncFuture<ITransport> send( IModelObject message) throws IOException
  {
    message.setAttribute( "route", at);
    return via.send( message);
  }
  
  @Override
  public AsyncFuture<ITransport> send( IModelObject message, IContext messageContext, int timeout) throws IOException
  {
    message.setAttribute( "route", at);
    return via.send( message, messageContext, timeout);
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
  public void addListener( ITimeoutListener listener)
  {
    via.addListener( listener);
  }
  
  @Override
  public void removeListener( ITimeoutListener listener)
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

  private ITransport via;
  private String at;
}
