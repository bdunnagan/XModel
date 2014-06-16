package org.xmodel.net.nu;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.xpath.expression.IContext;

public final class RoutedTransport implements ITransport, IConnectListener, IDisconnectListener, IReceiveListener, IErrorListener
{
  public RoutedTransport( ITransportImpl via, String at)
  {
    this.notifier = new TransportNotifier();
    this.via = via;
    this.at = at;
  }

  @Override
  public void onConnect( ITransport transport, IContext context) throws Exception
  {
    notifier.notifyConnect( this, context);
  }

  @Override
  public void onDisconnect( ITransport transport, IContext context) throws Exception
  {
    notifier.notifyDisconnect( this, context);
  }

  @Override
  public void onReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject request) throws Exception
  {
    notifier.notifyReceive( this, message, messageContext, request);
  }

  @Override
  public void onError( ITransport transport, IContext context, Error error, IModelObject request) throws Exception
  {
    notifier.notifyError( this, context, error, request);
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
    notifier.addListener( listener);
  }
  
  @Override
  public void removeListener( IReceiveListener listener)
  {
    notifier.removeListener( listener);
  }
  
  @Override
  public void addListener( IConnectListener listener)
  {
    notifier.addListener( listener);
  }
  
  @Override
  public void removeListener( IConnectListener listener)
  {
    notifier.removeListener( listener);
  }
  
  @Override
  public void addListener( IDisconnectListener listener)
  {
    notifier.addListener( listener);
  }
  
  @Override
  public void removeListener( IDisconnectListener listener)
  {
    notifier.removeListener( listener);
  }

  @Override
  public void addListener( IErrorListener listener)
  {
    notifier.addListener( listener);
  }
  
  @Override
  public void removeListener( IErrorListener listener)
  {
    notifier.removeListener( listener);
  }

  private TransportNotifier notifier;
  private ITransportImpl via;
  private String at;
}
