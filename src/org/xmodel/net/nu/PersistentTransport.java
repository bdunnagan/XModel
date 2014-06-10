package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledFuture;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

public class PersistentTransport implements ITransportImpl, IConnectListener, IDisconnectListener, IErrorListener, Runnable
{
  public PersistentTransport( ITransportImpl transport)
  {
    // invoke listeners with this transport
    transport.getNotifier().setTransport( this);
    
    transport.addListener( (IConnectListener)this);
    transport.addListener( (IDisconnectListener)this);
    transport.addListener( (IErrorListener)this);
    
    this.transport = transport;
    this.retryMinDelay = 1000;
    this.retryMaxDelay = 10000;
    this.retryDelay = retryMinDelay;
  }

  @Override
  public void onError( ITransport transport, IContext context, Error error, IModelObject request) throws Exception
  {
    switch( error)
    {
      case connectRefused:
      case connectError:
        this.transport.schedule( this, retryDelay);
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
    this.transport.schedule( this, retryDelay);
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
  public AsyncFuture<ITransport> request( IModelObject message, IContext messageContext, int timeout)
  {
    return transport.request( message, messageContext, timeout);
  }

  @Override
  public AsyncFuture<ITransport> ack( IModelObject request)
  {
    return transport.ack( request);
  }

  @Override
  public AsyncFuture<ITransport> respond( IModelObject message, IModelObject request)
  {
    return transport.respond( message, request);
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
  
  @Override
  public ScheduledFuture<?> schedule( Runnable runnable, int delay)
  {
    return transport.schedule( runnable, delay);
  }

  @Override
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope)
  {
    return transport.sendImpl( envelope);
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
  public TransportNotifier getNotifier()
  {
    return transport.getNotifier();
  }

  @Override
  public boolean notifyReceive( byte[] bytes, int offset, int length) throws IOException
  {
    return transport.notifyReceive( bytes, offset, length);
  }

  @Override
  public boolean notifyReceive( ByteBuffer buffer) throws IOException
  {
    return transport.notifyReceive( buffer);
  }

  @Override
  public void notifyReceive( IModelObject message, IContext messageContext, IModelObject request)
  {
    transport.notifyReceive( message, messageContext, request);
  }

  @Override
  public void notifyError( IContext context, Error error, IModelObject request)
  {
    transport.notifyError( context, error, request);
  }

  @Override
  public void notifyConnect()
  {
    transport.notifyConnect();
  }

  @Override
  public void notifyDisconnect()
  {
    transport.notifyDisconnect();
  }

  private ITransportImpl transport;
  private int connectTimeout;
  private int retryDelay;
  private int retryMinDelay;
  private int retryMaxDelay;
}
