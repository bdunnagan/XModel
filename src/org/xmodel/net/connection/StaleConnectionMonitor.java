package org.xmodel.net.connection;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.xmodel.future.AsyncFuture;
import org.xmodel.log.SLog;

/**
 * This class insures that the connection does not become idle by sending echo request messages over the connection.
 */
public class StaleConnectionMonitor implements INetworkConnection.IListener
{
  /**
   * Constructor.
   * @param idleSendTimeout The timeout in milliseconds after which an echo request will be sent when connection is idle.
   * @param staleTimeout The timeout in milliseconds after which an idle connection will be declared stale.
   * @param messageFactory A message factory for creating echo request messages.
   * @param connection The network connection to be monitored.
   * @param scheduler The scheduler for scheduling the timeouts.
   */
  public StaleConnectionMonitor( int idleSendTimeout, int staleTimeout, INetworkMessageFactory messageFactory, INetworkConnection connection, ScheduledExecutorService scheduler)
  {
    this.idleSendTimeout = idleSendTimeout;
    this.staleTimeout = staleTimeout;
    this.messageFactory = messageFactory;
    this.connection = connection;
    this.scheduler = scheduler;
    
    staleFuture = new AsyncFuture<StaleConnectionMonitor>( this);
    
    idleSendFutureRef = new AtomicReference<ScheduledFuture<?>>();
    timeoutFutureRef = new AtomicReference<ScheduledFuture<?>>();
    closed = new AtomicBoolean();
    
    connection.connect().addListener( new AsyncFuture.IListener<INetworkConnection>() {
      public void notifyComplete( AsyncFuture<INetworkConnection> future) throws Exception
      {
        scheduleHeartbeatMessage();
      }
    });
  }
  
  /**
   * @return Returns the future that is notified when the connection is lost.
   */
  public AsyncFuture<StaleConnectionMonitor> getStaleFuture()
  {
    return staleFuture;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection.IListener#onMessageReceived(org.xmodel.net.connection.INetworkConnection, org.xmodel.net.connection.INetworkMessage)
   */
  @Override
  public void onMessageReceived( INetworkConnection connection, INetworkMessage message)
  {
    if ( closed.get()) return;
    
    scheduleHeartbeatTimeout();
    scheduleHeartbeatMessage();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection.IListener#onClose(org.xmodel.net.connection.INetworkConnection, java.lang.Object)
   */
  @Override
  public void onClose( INetworkConnection connection, Object cause)
  {
    closed.set( true);
    
    ScheduledFuture<?> future = timeoutFutureRef.getAndSet( null);
    if ( future != null) future.cancel( false);
    
    future = idleSendFutureRef.getAndSet( null);
    if ( future != null) future.cancel( false);
  }

  /**
   * Schedule the heart-beat timeout.
   */
  private void scheduleHeartbeatTimeout()
  {
    if ( closed.get()) return;
    
    ScheduledFuture<?> future = timeoutFutureRef.get();
    if ( future != null) future.cancel( false);
    timeoutFutureRef.set( scheduler.schedule( timeoutTask, staleTimeout, TimeUnit.MILLISECONDS));
  }

  /**
   * Schedule the next heart-beat message.
   */
  private void scheduleHeartbeatMessage()
  {
    if ( closed.get()) return;
    
    ScheduledFuture<?> future = idleSendFutureRef.get();
    if ( future != null) future.cancel( false);
    idleSendFutureRef.set( scheduler.schedule( idleSendTask, idleSendTimeout, TimeUnit.MILLISECONDS));
  }
  
  private Runnable idleSendTask = new Runnable() {
    public void run()
    {
      if ( closed.get()) return;
      
      try
      {
        connection.send( messageFactory.createEchoRequest());
        idleSendFutureRef.set( null);
        scheduleHeartbeatMessage();
      }
      catch( Exception e)
      {
        SLog.exception( this, e);
      }
    }
  };
  
  private Runnable timeoutTask = new Runnable() {
    public void run()
    {
      if ( closed.get()) return;
      
      staleFuture.notifySuccess();
    }
  };

  private INetworkConnection connection;
  private INetworkMessageFactory messageFactory;
  private ScheduledExecutorService scheduler;
  private int staleTimeout;
  private AtomicReference<ScheduledFuture<?>> timeoutFutureRef;
  private int idleSendTimeout;
  private AtomicReference<ScheduledFuture<?>> idleSendFutureRef;
  private AsyncFuture<StaleConnectionMonitor> staleFuture;
  private AtomicBoolean closed;
}
