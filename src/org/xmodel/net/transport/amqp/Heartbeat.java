package org.xmodel.net.transport.amqp;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.xmodel.net.XioPeer;

public class Heartbeat
{
  public Heartbeat( XioPeer peer, int period, int timeout, Executor executor)
  {
    this.peer = peer;
    this.period = period;
    this.timeout = timeout;
    this.executor = executor;
  }

  /**
   * Start sending heartbeat messages.
   */
  public void start()
  {
    heartbeatTask = new Runnable() {
      public void run()
      {
        executor.execute( new Runnable() {
          public void run()
          {
            try { peer.heartbeat();} catch( Exception e) {}
          }
        });
      }
    };
    
    heartbeatFuture = scheduler.scheduleAtFixedRate( heartbeatTask, period, period, TimeUnit.MILLISECONDS);
    
    timeoutTask = new Runnable() {
      public void run()
      {
        executor.execute( new Runnable() {
          public void run()
          {
            stop();
            //peer.getPeerRegistry().unregisterAll( peer);
            peer.close();
          }
        });
      }
    };
    
    timeoutFuture = scheduler.schedule( timeoutTask, timeout, TimeUnit.MILLISECONDS);
  }
  
  /**
   * Stop sending heartbeat messages.
   */
  public void stop()
  {
    heartbeatFuture.cancel( false);
  }
  
  /**
   * Call this method when a message is received.
   */
  public void messageReceived()
  {
    // TODO: signal to not send heartbeat message until idle
    
    if ( timeoutFuture == null || timeoutFuture.cancel( false))
    {
      timeoutFuture = scheduler.schedule( timeoutTask, timeout, TimeUnit.MILLISECONDS);
    }
  }
  
  private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( 1);

  private XioPeer peer;
  private int period;
  private int timeout;
  private Executor executor;
  private Runnable heartbeatTask;
  private ScheduledFuture<?> heartbeatFuture;
  private Runnable timeoutTask;
  private ScheduledFuture<?> timeoutFuture;
}
