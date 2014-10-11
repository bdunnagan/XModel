package org.xmodel.net.nu.algo;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;
import org.xmodel.net.nu.DefaultEventHandler;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol.Type;
import org.xmodel.xpath.expression.IContext;

public class HeartbeatAlgo extends DefaultEventHandler
{
  public HeartbeatAlgo( ITransportImpl transport, int period, int timeout, ScheduledExecutorService scheduler)
  {
    this.transport = transport;
    this.period = period;
    this.timeout = timeout;
    this.scheduler = scheduler;
    this.heartbeatFutureRef = new AtomicReference<ScheduledFuture<?>>();
    this.timeoutFutureRef = new AtomicReference<ScheduledFuture<?>>();

    log.setLevel( Log.all);
  }
  
  @Override
  public boolean notifyReceive( ITransportImpl transport, IModelObject envelope)
  {
    resetHeartbeatTimeout();
    
    if ( transport.getProtocol().envelope().getType( envelope) == Type.heartbeat)
    {
      transport.sendAck( envelope);
      return true;
    }
    return false;
  }

  @Override
  public boolean notifyConnect( ITransportImpl transport, IContext transportContext) throws IOException
  {
    resetHeartbeatTimeout();
    startHeartbeat();
    return false;
  }

  @Override
  public boolean notifyDisconnect( ITransportImpl transport, IContext transportContext) throws IOException
  {
    stopHeartbeat();
    stopHeartbeatTimeout();
    
    return false;
  }

  @Override
  public boolean notifyError( ITransportImpl transport, IContext context, Error error, IModelObject request)
  {
    if ( error == Error.heartbeatLost)
    {
      SLog.errorf( this, "Lost heartbeat on transport, %s", transport);
      transport.disconnect();
      return true;
    }

    return false;
  }

  private void startHeartbeat()
  {
    log.verbosef( "Starting heartbeat for transport, %s, period=%d", transport, period);
    heartbeatFutureRef.set( scheduler.schedule( heartbeatRunnable, period, TimeUnit.MILLISECONDS));
  }
  
  private void stopHeartbeat()
  {
    log.verbosef( "Stopping heartbeat for transport, %s", transport);
    ScheduledFuture<?> heartbeatFuture = heartbeatFutureRef.get();
    if ( heartbeatFuture != null) heartbeatFuture.cancel( false);
  }
  
  private void resetHeartbeatTimeout()
  {
    log.verbosef( "Reset heartbeat timeout for transport, %s, timeout=%d", transport, timeout);
    ScheduledFuture<?> timeoutFuture = timeoutFutureRef.get();
    if ( timeoutFuture == null || timeoutFuture.cancel( false))
    {
      timeoutFutureRef.set( scheduler.schedule( timeoutRunnable, timeout, TimeUnit.MILLISECONDS));
    }
  }
  
  private void stopHeartbeatTimeout()
  {
    ScheduledFuture<?> timeoutFuture = timeoutFutureRef.get();
    if ( timeoutFuture != null) timeoutFuture.cancel( false);
    timeoutFutureRef.set( null);
  }
  
  private final Runnable heartbeatRunnable = new Runnable() {
    public void run()
    {
      log.verbosef( "Sending heartbeat for transport, %s, period=%d", transport, period);
      IModelObject envelope = transport.getProtocol().envelope().buildHeartbeatEnvelope();
      transport.sendImpl( envelope, null);
      heartbeatFutureRef.set( scheduler.schedule( heartbeatRunnable, period, TimeUnit.MILLISECONDS));
    }
  };
  
  private final Runnable timeoutRunnable = new Runnable() {
    public void run()
    {
      log.verbosef( "Heartbeat timeout expired for transport, %s, timeout=%d", transport, timeout);
      transport.getEventPipe().notifyError( transport, transport.getTransportContext(), Error.heartbeatLost, null);
    }
  };

  public final static Log log = Log.getLog( HeartbeatAlgo.class);
  
  private ITransportImpl transport;
  private int period;
  private int timeout;
  private ScheduledExecutorService scheduler;
  private AtomicReference<ScheduledFuture<?>> heartbeatFutureRef;
  private AtomicReference<ScheduledFuture<?>> timeoutFutureRef;
}
