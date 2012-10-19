package org.xmodel.net;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.xmodel.log.Log;

/**
 * The ping (heartbeat) protocol is used to provide timely notification of loss of connectivity.
 */
class Ping implements Runnable
{
  public Ping( ProtocolOld protocol, ILink link, int timeout)
  {
    this.protocol = protocol;
    this.link = link;
    this.alive = true;
    this.timer = scheduler.scheduleAtFixedRate( this, 0, timeout, TimeUnit.MILLISECONDS);
  }

  /**
   * Stop pinging.
   */
  public void stop()
  {
    ScheduledFuture<?> timer;
    synchronized( this) 
    { 
      timer = this.timer;
      this.timer = null;
    }
    
    if ( timer != null) timer.cancel( false);
  }
  
  /**
   * Call this method to inform Ping that a message was received.
   */
  protected synchronized void onMessageReceived()
  {
    log.debugf( "Ping refreshed for %s", link);
    alive = true;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run()
  {
    if ( !alive)
    {
      log.warnf( "Ping closing for %s", link);
      stop();
      link.close();
    }
    else
    {
      log.debugf( "Ping sent for %s", link);
      
      try
      {
        alive = false;
        protocol.sendPingRequest( link);
      }
      catch( IOException e)
      {
        log.infof( "Failed to send ping request, %s", e.getMessage());
        stop();
      }
    }
  }
  
  private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( 1);
  private static Log log = Log.getLog( Ping.class);

  private ProtocolOld protocol;
  private volatile ILink link;
  private volatile boolean alive;
  private ScheduledFuture<?> timer;
}
