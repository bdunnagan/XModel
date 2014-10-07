package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol.Type;
import org.xmodel.xpath.expression.IContext;

public class Heartbeater implements IEventHandler
{
  public Heartbeater( ITransportImpl transport, int period, int timeout)
  {
    this.transport = transport;
    this.period = period;
    this.timeout = timeout;
    this.heartbeatFutureRef = new AtomicReference<ScheduledFuture<?>>();
    this.timeoutFutureRef = new AtomicReference<ScheduledFuture<?>>();

    log.setLevel( Log.all);
  }
  
  @Override
  public boolean notifyReceive( ByteBuffer buffer) throws IOException
  {
    return false;
  }

  @Override
  public boolean notifyReceive( IModelObject envelope)
  {
    if ( transport.getProtocol().envelope().getType( envelope) == Type.heartbeat)
    {
      resetHeartbeatTimeout();
      return true;
    }
    
    return false;
  }

  @Override
  public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage)
  {
    return false;
  }

  @Override
  public boolean notifyConnect( IContext transportContext) throws IOException
  {
    resetHeartbeatTimeout();
    startHeartbeat();
    return false;
  }

  @Override
  public boolean notifyDisconnect( IContext transportContext) throws IOException
  {
    stopHeartbeat();
    
    ScheduledFuture<?> timeoutFuture = timeoutFutureRef.get();
    if ( timeoutFuture != null) timeoutFuture.cancel( false);
    timeoutFutureRef.set( null);
    
    return false;
  }

  @Override
  public boolean notifyError( IContext context, Error error, IModelObject request)
  {
    if ( error == Error.heartbeatLost)
    {
      SLog.errorf( this, "Lost heartbeat on transport, %s", transport);
      transport.disconnect();
      return true;
    }

    return false;
  }

  @Override
  public boolean notifyException( IOException e)
  {
    return false;
  }
  
  private void startHeartbeat()
  {
    log.verbosef( "Starting heartbeat for transport, %s, period=%d", transport, period);
    heartbeatFutureRef.set( transport.schedule( heartbeatRunnable, period));
  }
  
  private void stopHeartbeat()
  {
    log.verbosef( "Stopping heartbeat for transport, %s", transport);
    ScheduledFuture<?> heartbeatFuture = heartbeatFutureRef.get();
    if ( heartbeatFuture != null) heartbeatFuture.cancel( false);
  }
  
  private void resetHeartbeatTimeout()
  {
    ScheduledFuture<?> timeoutFuture = timeoutFutureRef.get();
    if ( timeoutFuture == null || timeoutFuture.cancel( false))
    {
      timeoutFutureRef.set( transport.schedule( timeoutRunnable, timeout));
    }
  }
  
  private final Runnable heartbeatRunnable = new Runnable() {
    public void run()
    {
      log.verbosef( "Sending heartbeat for transport, %s, period=%d", transport, period);
      IModelObject envelope = transport.getProtocol().envelope().buildHeartbeatEnvelope();
      transport.sendImpl( envelope, null);
    }
  };
  
  private final Runnable timeoutRunnable = new Runnable() {
    public void run()
    {
      log.verbosef( "Heartbeat timeout expired for transport, %s, timeout=%d", transport, timeout);
      transport.getEventPipe().notifyError( transport.getTransportContext(), Error.heartbeatLost, null);
    }
  };

  public final static Log log = Log.getLog( Heartbeater.class);
  
  private ITransportImpl transport;
  private int period;
  private int timeout;
  private AtomicReference<ScheduledFuture<?>> heartbeatFutureRef;
  private AtomicReference<ScheduledFuture<?>> timeoutFutureRef;
}
