package org.xmodel.net.nu.algo;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.DefaultEventHandler;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.xpath.expression.IContext;

public class ReconnectAlgo extends DefaultEventHandler
{
  public ReconnectAlgo( ScheduledExecutorService scheduler)
  {
    this.scheduler = scheduler;
    this.retryMinDelay = 1000;
    this.retryMaxDelay = 10000;
    this.retryDelay = retryMinDelay;
  }

  @Override
  public boolean notifyConnect( ITransportImpl transport, IContext transportContext) throws IOException
  {
    retryDelay = retryMinDelay;
    return false;
  }

  @Override
  public boolean notifyDisconnect( ITransportImpl transport, IContext transportContext) throws IOException
  {
    scheduler.schedule( new ReconnectRunnable( transport), retryDelay, TimeUnit.MILLISECONDS);
    increaseRetryDelay();
    return false;
  }

  private void increaseRetryDelay()
  {
    retryDelay *= 2;
    if ( retryDelay > retryMaxDelay) retryDelay = retryMaxDelay;
  }
  
  @Override
  public boolean notifyError( ITransportImpl transport, IContext context, Error error, IModelObject request)
  {
    switch( error)
    {
      case connectRefused:
      case connectError:
        scheduler.schedule( new ReconnectRunnable( transport), retryDelay, TimeUnit.MILLISECONDS);
        increaseRetryDelay();
        break;
      
      default:
        break;
    }
    
    return false;
  }
  
  private static class ReconnectRunnable implements Runnable
  {
    public ReconnectRunnable( ITransportImpl transport)
    {
      this.transport = transport;
    }
    
    @Override
    public void run()
    {
      transport.connect();
    }

    private ITransportImpl transport;
  }

  private ScheduledExecutorService scheduler;
  private int retryDelay;
  private int retryMinDelay;
  private int retryMaxDelay;
}
