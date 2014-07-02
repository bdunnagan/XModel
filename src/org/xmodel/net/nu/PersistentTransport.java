package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledFuture;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

public class PersistentTransport implements ITransportImpl, IEventHandler, Runnable
{
  public PersistentTransport( ITransportImpl transport)
  {
    notifier = new TransportNotifier();
    
    this.transport = transport;
    this.retryMinDelay = 1000;
    this.retryMaxDelay = 10000;
    this.retryDelay = retryMinDelay;
    
    transport.setEventHandler( this);
  }

  @Override
  public void onConnect( ITransport transport, IContext context) throws Exception
  {
    retryDelay = retryMinDelay;
    notifier.notifyConnect( this, context);
  }

  @Override
  public void onDisconnect( ITransport transport, IContext context) throws Exception
  {
    this.transport.schedule( this, retryDelay);
    increaseRetryDelay();
    
    notifier.notifyDisconnect( this, context);
  }
  
  private void increaseRetryDelay()
  {
    retryDelay *= 2;
    if ( retryDelay > retryMaxDelay) retryDelay = retryMaxDelay;
  }
  
  @Override
  public void onReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject request) throws Exception
  {
    notifier.notifyReceive( this, message, messageContext, request);
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
    
    notifier.notifyError( this, context, error, request);
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
  public void notifyReceive( ByteBuffer buffer) throws IOException
  {
    transport.notifyReceive( buffer);
  }

  @Override
  public void notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage)
  {
  }

  @Override
  public void notifyConnect() throws IOException
  {
    retryDelay = retryMinDelay;
    notifier.notifyConnect( this, transport.getTransportContext());
    
    transport.notifyConnect();
  }

  @Override
  public void notifyDisconnect() throws IOException
  {
    transport.notifyDisconnect();
  }

  @Override
  public void notifyError( IContext context, Error error, IModelObject request)
  {
    transport.notifyError( context, error, request);
  }

  @Override
  public void notifyException( IOException e)
  {
  }

  private ITransportImpl transport;
  private TransportNotifier notifier;
  private int connectTimeout;
  private int retryDelay;
  private int retryMinDelay;
  private int retryMaxDelay;
}
