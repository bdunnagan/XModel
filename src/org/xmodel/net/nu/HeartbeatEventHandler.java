package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.xmodel.IModelObject;
import org.xmodel.log.SLog;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol.Type;
import org.xmodel.xpath.expression.IContext;

public class HeartbeatEventHandler implements IEventHandler
{
  public HeartbeatEventHandler( ITransportImpl transport, int period, int timeout)
  {
    this.transport = transport;
    this.period = period;
    this.timeout = timeout;
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
    heartbeatFutureRef.set( transport.schedule( heartbeatRunnable, period));
  }
  
  private void stopHeartbeat()
  {
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
      IModelObject envelope = transport.getProtocol().envelope().buildHeartbeatEnvelope();
      transport.sendImpl( envelope, null);
    }
  };
  
  private final Runnable timeoutRunnable = new Runnable() {
    public void run()
    {
      transport.getEventPipe().notifyError( transport.getTransportContext(), Error.heartbeatLost, null);
    }
  };
  
  private ITransportImpl transport;
  private int period;
  private int timeout;
  private AtomicReference<ScheduledFuture<?>> heartbeatFutureRef;
  private AtomicReference<ScheduledFuture<?>> timeoutFutureRef;
}
