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
  public Heartbeat( AmqpXioPeer peer, int count, int timeout, Executor executor, boolean isClient)
  {
    this.peer = peer;
    this.period = timeout / (count + 1) + 1000;
    this.timeout = timeout;
    this.executor = executor;
    this.isClient = isClient;
    this.active = new AtomicBoolean( false);
    
    heartbeatTask = new Runnable() {
      public void run()
      {
        heartbeat();
      }
    };
    
    timeoutTask = new Runnable() {
      public void run()
      {
        timeout();
      }
    };
  }

  /**
   * Start sending heartbeat messages.
   */
  public synchronized void start()
  {
    if ( isClient)
    {
      active.set( true);
      heartbeatFuture = scheduler.scheduleAtFixedRate( heartbeatTask, 0, period, TimeUnit.MILLISECONDS);
    }
    
    timeoutFuture = scheduler.schedule( timeoutTask, timeout, TimeUnit.MILLISECONDS);
  }
  
  /**
   * Stop sending heartbeat messages.
   */
  public synchronized void stop()
  {
    if ( timeoutFuture != null) 
    {
      timeoutFuture.cancel( false);
    }
    
    if ( active.getAndSet( false) && heartbeatFuture != null)
    {
      heartbeatFuture.cancel( false);
    }
  }
  
  /**
   * Called when the heartbeat schedule expires.
   */
  private void heartbeat()
  {
    executor.execute( new Runnable() {
      public void run()
      {
        try { peer.heartbeat();} catch( Exception e) { SLog.exception( this, e);}
      }
    });
  }
  
  /**
   * Called when the timeout schedule expires.
   */
  private void timeout()
  {
    executor.execute( new Runnable() {
      public void run()
      {
        // NOTE: server must send message after registration to restart heartbeat!
        peer.close();
        
        if ( isClient)
        {
          try
          {
            peer.reregister();
          }
          catch( Exception e)
          {
            SLog.error( this, String.format( "Unable to re-register after heartbeat lost, %s", peer), e);
          }
        }
        else
        {
          peer.getPeerRegistry().unregisterAll( peer);
        }
      }
    });
  }
  
  /**
   * Call this method when a message is received.
   */
  public synchronized void messageReceived()
  {
    if ( isClient && !active.get())
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
  private boolean isClient;
  private AtomicBoolean active;
}
