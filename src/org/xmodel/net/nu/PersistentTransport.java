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
    this.transport = transport;
    transport.getEventPipe().addLast( this);
    
    this.retryMinDelay = 1000;
    this.retryMaxDelay = 10000;
    this.retryDelay = retryMinDelay;
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
  public AsyncFuture<ITransport> register( String name, IContext messageContext, int timeout)
  {
    return transport.register( name, messageContext, timeout);
  }

  @Override
  public AsyncFuture<ITransport> deregister( String name, IContext messageContext, int timeout)
  {
    return transport.deregister( name, messageContext, timeout);
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
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope, IModelObject request)
  {
    return transport.sendImpl( envelope, null);
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
  public EventPipe getEventPipe()
  {
    return transport.getEventPipe();
  }

  @Override
  public boolean notifyReceive( ByteBuffer buffer) throws IOException
  {
    return false;
  }

  @Override
  public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage)
  {
    return false;
  }

  @Override
  public boolean notifyConnect(IContext transportContext) throws IOException
  {
    retryDelay = retryMinDelay;
    return false;
  }

  @Override
  public boolean notifyDisconnect(IContext transportContext) throws IOException
  {
    this.transport.schedule( this, retryDelay);
    increaseRetryDelay();
    return false;
  }

  private void increaseRetryDelay()
  {
    retryDelay *= 2;
    if ( retryDelay > retryMaxDelay) retryDelay = retryMaxDelay;
  }
  
  @Override
  public boolean notifyError( IContext context, Error error, IModelObject request)
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
    
    return false;
  }

  @Override
  public boolean notifyException( IOException e)
  {
    return false;
  }

  private ITransportImpl transport;
  private int connectTimeout;
  private int retryDelay;
  private int retryMinDelay;
  private int retryMaxDelay;
}
