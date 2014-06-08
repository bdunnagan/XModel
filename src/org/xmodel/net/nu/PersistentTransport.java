package org.xmodel.net.nu;

import java.util.concurrent.ScheduledFuture;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.xpath.expression.IContext;

public class PersistentTransport implements ITransport, IConnectListener, IDisconnectListener, IErrorListener, Runnable
{
  public PersistentTransport( ITransport transport)
  {
    transport.addListener( (IConnectListener)this);
    transport.addListener( (IDisconnectListener)this);
    transport.addListener( (IErrorListener)this);
    
    this.transport = transport;
    this.retryMinDelay = 1000;
    this.retryMaxDelay = 10000;
    this.retryDelay = retryMinDelay;
  }

  @Override
  public void onError( ITransport transport, IContext context, Error error) throws Exception
  {
    switch( error)
    {
      case connectRefused:
      case connectError:
        schedule( this, retryDelay);
        increaseRetryDelay();
        break;
      
      default:
        break;
    }
  }

  @Override
  public void onConnect( ITransport transport, IContext context) throws Exception
  {
    retryDelay = retryMinDelay;
  }

  @Override
  public void onDisconnect( ITransport transport, IContext context) throws Exception
  {
    schedule( this, retryDelay);
    increaseRetryDelay();
  }
  
  private void increaseRetryDelay()
  {
    retryDelay *= 2;
    if ( retryDelay > retryMaxDelay) retryDelay = retryMaxDelay;
  }
  
  @Override
  public void run()
  {
    connect( connectTimeout);
  }
  
  @Override
  public AsyncFuture<ITransport> connect( int timeout)
  {
    connectTimeout = timeout;
    return transport.connect( timeout);
  }

  @Override
  public AsyncFuture<ITransport> disconnect()
  {
    return transport.disconnect();
  }

  @Override
  public AsyncFuture<ITransport> send( IModelObject message)
  {
    return transport.send( message);
  }

  @Override
  public AsyncFuture<ITransport> request( IModelObject message, IContext messageContext, int timeout)
  {
    return transport.request( message, messageContext, timeout);
  }

  @Override
  public AsyncFuture<ITransport> respond( IModelObject message, IModelObject request)
  {
    return transport.respond( message, request);
  }

  @Override
  public ScheduledFuture<?> schedule( Runnable runnable, int delay)
  {
    return transport.schedule( runnable, delay);
  }

  @Override
  public void addListener( IConnectListener listener)
  {
    transport.addListener( listener);
  }

  @Override
  public void removeListener( IConnectListener listener)
  {
    transport.removeListener( listener);
  }

  @Override
  public void addListener( IDisconnectListener listener)
  {
    transport.addListener( listener);
  }

  @Override
  public void removeListener( IDisconnectListener listener)
  {
    transport.removeListener( listener);
  }

  @Override
  public void addListener( IReceiveListener listener)
  {
    transport.addListener( listener);
  }

  @Override
  public void removeListener( IReceiveListener listener)
  {
    transport.removeListener( listener);
  }

  @Override
  public void addListener( IErrorListener listener)
  {
    transport.addListener( listener);
  }

  @Override
  public void removeListener( IErrorListener listener)
  {
    transport.removeListener( listener);
  }
  
  private ITransport transport;
  private int connectTimeout;
  private int retryDelay;
  private int retryMinDelay;
  private int retryMaxDelay;
}
