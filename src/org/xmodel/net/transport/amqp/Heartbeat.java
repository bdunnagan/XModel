package org.xmodel.net.transport.amqp;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xmodel.log.SLog;

public class Heartbeat
{
  public Heartbeat( AmqpXioPeer peer, int period, int timeout, Executor executor)
  {
    this.peer = peer;
    this.period = period;
    this.timeout = timeout;
    this.executor = executor;
    this.active = new AtomicBoolean( false);
  }

  /**
   * Start sending heartbeat messages.
   */
  public void start()
  {
    active.set( true);
    
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
            // NOTE: server must send message after registration to restart heartbeat!
            stop();
            
            try
            {
              peer.reregister();
            }
            catch( Exception e)
            {
              SLog.exception( String.format( "Unable to re-register after heartbeat lost, %s", peer), e);
            }
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
    if ( active.getAndSet( false))
    {
      heartbeatFuture.cancel( false);
      heartbeatTask = timeoutTask = null;
    }
  }
  
  /**
   * Call this method when a message is received.
   */
  public void messageReceived()
  {
    if ( !active.get())
    {
      start();
    }
    
    // TODO: signal to not send heartbeat message until idle
    if ( timeoutFuture == null || timeoutFuture.cancel( false))
    {
      timeoutFuture = scheduler.schedule( timeoutTask, timeout, TimeUnit.MILLISECONDS);
    }
  }
  
  private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( 1);

  private AmqpXioPeer peer;
  private int period;
  private int timeout;
  private Executor executor;
  private Runnable heartbeatTask;
  private ScheduledFuture<?> heartbeatFuture;
  private Runnable timeoutTask;
  private ScheduledFuture<?> timeoutFuture;
  private AtomicBoolean active;
}
