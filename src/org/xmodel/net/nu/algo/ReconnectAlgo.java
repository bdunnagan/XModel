package org.xmodel.net.nu.algo;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.xmodel.IModelObject;
import org.xmodel.net.nu.DefaultEventHandler;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.xpath.expression.IContext;

public class ReconnectAlgo extends DefaultEventHandler implements Runnable
{
  public ReconnectAlgo( ITransportImpl transport, ScheduledExecutorService scheduler)
  {
    this.transport = transport;
    this.scheduler = scheduler;
    this.retryMinDelay = 1000;
    this.retryMaxDelay = 10000;
    this.retryDelay = retryMinDelay;
  }

  @Override
  public void run()
  {
    transport.connect();
  }
  
  @Override
  public boolean notifyConnect( IContext transportContext) throws IOException
  {
    retryDelay = retryMinDelay;
    return false;
  }

  @Override
  public boolean notifyDisconnect( IContext transportContext) throws IOException
  {
    scheduler.schedule( this, retryDelay, TimeUnit.MILLISECONDS);
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
        scheduler.schedule( this, retryDelay, TimeUnit.MILLISECONDS);
        increaseRetryDelay();
        break;
      
      default:
        break;
    }
    
    return false;
  }

  private ITransportImpl transport;
  private ScheduledExecutorService scheduler;
  private int retryDelay;
  private int retryMinDelay;
  private int retryMaxDelay;
}
