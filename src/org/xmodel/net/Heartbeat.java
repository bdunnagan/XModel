package org.xmodel.net;

import java.io.IOException;
import org.xmodel.IDispatcher;
import org.xmodel.log.SLog;

/**
 * Implementation of heartbeat for timely notification of loss of connectivity in either direction.
 */
public class Heartbeat
{
  public final static int heartbeatPeriod = 3000;
  public final static int connectionTimeout = 10000;

  public static interface IListener
  {
    /**
     * Called when a new ping round-trip delay is calculated.
     * @param delay The ping delay in milliseconds.
     */
    public void onActivity( float delay);
    
    /**
     * Called just before the link is closed due to heartbeat timeout.
     */
    public void onLinkDown();
  }
  
  /**
   * Create a Heartbeat that uses the specified session.
   * @param session The heartbeat session.
   * @param listener The link down listener.
   * @param dispatcher The dispatcher.
   */
  public Heartbeat( Session session, IListener listener, IDispatcher dispatcher)
  {
    this.session = session;
    this.listener = listener;
    this.dispatcher = dispatcher;
  }
  
  /**
   * Start the heartbeat.
   */
  public synchronized void start()
  {
    lastActivity = System.nanoTime();
    
    thread = new Thread( heartbeatRunnable, "Heartbeat");
    thread.setDaemon( true);
    thread.start();
  }
  
  /**
   * Stop the heartbeat.
   */
  public synchronized void stop()
  {
    thread.interrupt();
    thread = null;
  }
  
  /**
   * Called whenever a message is received.
   */
  public synchronized void onActivity()
  {
    lastActivity = System.nanoTime();
    
    float latency = 0;
    synchronized( this) { latency = (lastActivity - pingSent) / 1000000f;}
    
    listener.onActivity( latency);
  }
  
  private final Runnable heartbeatRunnable = new Runnable() {
    @Override public void run()
    {
      while( true)
      {
        float latency = 0;
        synchronized( this) 
        { 
          pingSent = System.nanoTime();
          latency = (pingSent - lastActivity) / 1000000f;
        }
        
        if ( latency > connectionTimeout) 
        {
          listener.onLinkDown();
          break;
        }
        else
        {
          dispatcher.execute( pingRunnable);
        }
        
        try
        {
          Thread.sleep( heartbeatPeriod);
        }
        catch( Exception e)
        {
          SLog.exception( this, e);
          break;
        }
      }
    }
  };
  
  private final Runnable pingRunnable = new Runnable() {
    @Override public void run()
    {
      try
      {
        session.ping();
      }
      catch( IOException e)
      {
        SLog.exception( this, e);
      }
    }
  };

  private Thread thread;
  private Session session;
  private IListener listener;
  private IDispatcher dispatcher;
  private long pingSent;
  private long lastActivity;
}
