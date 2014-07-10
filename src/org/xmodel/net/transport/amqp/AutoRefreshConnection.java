package org.xmodel.net.transport.amqp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.xmodel.log.Log;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * An AMQP Connection wrapper that automatically refreshes the underlying connection periodically.
 */
public class AutoRefreshConnection
{
  public AutoRefreshConnection( ConnectionFactory connectionFactory, ExecutorService executor, Address[] brokers) throws IOException
  {
    this.connectionFactory = connectionFactory;
    this.executor = executor;
    this.brokers = brokers;
    this.activeConnection = connectionFactory.newConnection( executor, brokers);
    this.threadItems = new ThreadLocal<ThreadItem>();
    this.allThreadItems = Collections.synchronizedList( new ArrayList<ThreadItem>());
    this.connectionLock = new ReentrantReadWriteLock( true);
    this.consumers = Collections.synchronizedMap( new HashMap<AmqpXioChannel, AutoRefreshConsumer>());
    this.obsoleteLeaseQueue = new LinkedBlockingQueue<Channel>();
  }

  /**
   * Start the schedule for periodically refreshing the connection.
   * @param period The period in seconds.
   * @param scheduler The scheduler.
   */
  public synchronized void startRefreshSchedule( int period, ScheduledExecutorService scheduler)
  {
    if ( refreshSchedule != null) stopRefreshSchedule();
    
    Runnable refreshRunnable = new Runnable() {
      public void run()
      {
        refresh();
      }
    };
    
    if ( period > 0)
    {
      refreshSchedule = scheduler.scheduleAtFixedRate( refreshRunnable, period, period, TimeUnit.SECONDS);
    }
  }
  
  /**
   * Stop the current refresh schedule.
   */
  public synchronized void stopRefreshSchedule()
  {
    if ( refreshSchedule != null) refreshSchedule.cancel( false);
    refreshSchedule = null;
  }
  
  /**
   * @return Returns true if the connection is open.
   */
  public boolean isOpen()
  {
    try
    {
      connectionLock.readLock().lock();
      return activeConnection != null && activeConnection.isOpen();
    }
    finally
    {
      connectionLock.readLock().unlock();
    }
  }
  
  /**
   * @return Returns the remote address.
   */
  public InetAddress getAddress()
  {
    try
    {
      connectionLock.readLock().lock();
      return (activeConnection != null)? activeConnection.getAddress(): null;
    }
    finally
    {
      connectionLock.readLock().unlock();
    }
  }

  /**
   * @return Returns the remote port.
   */
  public int getPort()
  {
    try
    {
      connectionLock.readLock().lock();
      return (activeConnection != null)? activeConnection.getPort(): -1;
    }
    finally
    {
      connectionLock.readLock().unlock();
    }
  }
  
  /**
   * Refresh the underlying connection.
   */
  private void refresh()
  {
    log.debug( "Refreshing connection ...");
    
    long t0 = System.nanoTime();

    //
    // Close previous connection.  If there are pending requests on this channel
    // they will be lost, so choose the refresh rate to be much slower than the longest
    // request processing time.  This is okay since the goal is just to refresh the 
    // connection frequently enough to prevent it being dropped by intermediate routers.
    //
    log.debug( "Closing obsolete connection ...");
    try
    {
      if ( obsoleteConnection != null) obsoleteConnection.close();
    }
    catch( IOException e)
    {
      log.warn( "Failed to close obsolete connection ...");
      log.exception( e);
    }
    
    // create new connection
    log.debug( "Swapping new connection ...");
    Connection newConnection = createConnection();
    
    // swap connection under write lock
    List<Channel> pendingLeases = new ArrayList<Channel>();
    long t1 = System.nanoTime();
    try
    {
      connectionLock.writeLock().lock();
      log.debugf( "Write lock acquired in %1.2fms", (System.nanoTime() - t1) / 1e6);

      // swap
      obsoleteConnection = activeConnection;
      activeConnection = newConnection;
      
      // clear channel of all thread items
      for( ThreadItem item: allThreadItems.toArray( new ThreadItem[ 0]))
      {
        if ( item.leased) pendingLeases.add( item.channel); 
        item.obsoleteChannel = item.channel;
        item.channel = null;
      }
    }
    finally
    {
      connectionLock.writeLock().unlock();
      log.debugf( "Write lock held for %1.2fms", (System.nanoTime() - t1) / 1e6);
    }
    
    // restart consumers
    log.debug( "Restarting consumers ...");
    for( Map.Entry<AmqpXioChannel, AutoRefreshConsumer> entry: consumers.entrySet())
    {
      Channel oldChannel = entry.getValue().getChannel();
      
      try
      {
        Channel channel = activeConnection.createChannel();
        
        AutoRefreshConsumer newConsumer = entry.getValue().cloneWithNewChannel( channel);
        consumers.put( entry.getKey(), newConsumer);
        
        channel.basicQos( 1);
        channel.basicConsume( newConsumer.queue, false, newConsumer.getConsumerTag(), newConsumer);
      }
      catch( IOException e)
      {
        log.warn( "Lost connection to message bus while refreshing connection ...");
        log.exception( e);

        recoverAfterRefreshFailure();
      }
      
      try
      {
        oldChannel.close();
      }
      catch( IOException e)
      {
        log.warn( "Lost connection to message bus while refreshing connection ...");
        log.exception( e);

        recoverAfterRefreshFailure();
      }
    }

    // wait for leased channels to be returned before closing old connection
    log.debug( "Waiting for leases to be returned ...");
    try
    {
      for( int i=0; i<pendingLeases.size(); i++)
        obsoleteLeaseQueue.take();
    }
    catch( InterruptedException e)
    {
      log.warn( "Interrupted while waiting for leases to be returned ...");
      log.exception( e);
    }

    // close channels
    log.debug( "Closing returned channels ...");
    for( Channel channel: pendingLeases)
    {
      try
      {
        channel.close();
      }
      catch( IOException e)
      {
        log.warn( "Failed to close channel during refresh.");
      }
    }
    
    log.debugf( "Connection refreshed in %1.3fs", (System.nanoTime() - t0) / 1e9);
  }
  
  /**
   * Recover after refresh failure.
   */
  private void recoverAfterRefreshFailure()
  {
    log.warn( "Recovering after refresh failure!");
    throw new UnsupportedOperationException();
  }
  
  /**
   * Lease a channel.
   * @return Returns the leased channel.
   */
  public Channel leaseChannel() throws IOException
  {
    try
    {
      connectionLock.readLock().lock();

      if ( activeConnection == null || !activeConnection.isOpen())
        throw new IOException( "Connection is closed.");
      
      ThreadItem item = threadItems.get();
      if ( item == null || item.channel == null)
      {
        log.debug( "Creating new channel");
        item = new ThreadItem();
        item.channel = activeConnection.createChannel();
        threadItems.set( item);
        allThreadItems.add( item);
      }
      
      item.leased = true;
      return item.channel;
    }
    finally
    {
      connectionLock.readLock().unlock();
    }
  }
  
  /**
   * Return a channel that was previously leased.
   * @param channel The channel.
   */
  public void returnChannel( Channel channel)
  {
    try
    {
      connectionLock.readLock().lock();
    
      ThreadItem item = threadItems.get();
      if ( channel == item.obsoleteChannel)
      {
        obsoleteLeaseQueue.offer( channel);
      }
      else if ( item.channel != channel) 
      {
        log.severe( "Incorrect thread channel returned.");
      }
      
      item.leased = false;
    }
    finally
    {
      connectionLock.readLock().unlock();
    }
  }
  
  /**
   * Start a consumer.
   * @param queue The queue.
   * @param durable True if durable.
   * @param autoDelete True if auto-delete.
   * @param purge True if input queue should be purged before consumer is started.
   * @param consumer The delegate consumer.
   */
  public void startConsumer( String queue, boolean durable, boolean autoDelete, boolean purge, AmqpXioChannel consumer) throws IOException
  {
    try
    {
      connectionLock.readLock().lock();
    
      Channel channel = activeConnection.createChannel();
      
      String tag = queue + counter.getAndIncrement();
      AutoRefreshConsumer refreshConsumer = new AutoRefreshConsumer( tag, queue, durable, autoDelete, channel, consumer);
      consumers.put( consumer, refreshConsumer);
      
      //
      // Setting qos to 1 to insure that consumer does not have backlog of messages
      // that may be interrupted during the connection refresh flow.  The consumer
      // flow is stopped before the consumer channel is closed.
      //
      channel.basicQos( 1);
      channel.queueDeclare( queue, durable, false, autoDelete, null);
      if ( purge) channel.queuePurge( queue);
      channel.basicConsume( queue, false, refreshConsumer.getConsumerTag(), refreshConsumer);
    }
    finally
    {
      connectionLock.readLock().unlock();
    }
  }
  
  /**
   * Stop the specified consumer.
   * @param consumer The consumer.
   */
  public void stopConsumer( AmqpXioChannel consumer) throws IOException
  {
    try
    {
      connectionLock.readLock().lock();
      
      AutoRefreshConsumer refreshConsumer = consumers.remove( consumer);
      refreshConsumer.getChannel().close();
    }
    finally
    {
      connectionLock.readLock().unlock();
    }
  }

  /**
   * Create a new connection.
   * @return Returns the new connection.
   */
  private Connection createConnection() 
  {
    while( true)
    {
      try
      {
        Connection connection = (brokers == null)?
            connectionFactory.newConnection( executor):
            connectionFactory.newConnection( executor, brokers);
            
        return connection;
      }
      catch( IOException e)
      {
        log.exception( e);
      }
      
      try { Thread.sleep( 3000);} catch( InterruptedException e) { break;}
    }
    
    return null;
  }

  /**
   * Close the connection.
   */
  public void close() throws IOException
  {
    try
    {
      connectionLock.writeLock().lock();
      
      closeChannels();
      
      if ( activeConnection != null) activeConnection.close();
      activeConnection = null;
      
      threadItems = new ThreadLocal<ThreadItem>();
      allThreadItems.clear();
      consumers.clear();
      obsoleteLeaseQueue = new LinkedBlockingQueue<Channel>();
    }
    finally
    {
      connectionLock.writeLock().unlock();
    }
  }
  
  /**
   * Close all open channels.
   */
  private void closeChannels()
  {
    try
    {
      connectionLock.writeLock().lock();

      for( ThreadItem item: allThreadItems)
      {
        try
        {
          if ( item.channel != null) item.channel.close();
          item.channel = null;
        }
        catch( IOException e)
        {
          log.warn( "Failed to close channel.");
        }
      }
    }
    finally
    {
      connectionLock.writeLock().unlock();
    }
  }

  private static class ThreadItem
  {
    public Channel channel;
    public Channel obsoleteChannel;
    public boolean leased;
  }
  
  private class AutoRefreshConsumer extends DefaultConsumer
  {
    public AutoRefreshConsumer( String tag, String queue, boolean durable, boolean autoDelete, Channel channel, AmqpXioChannel consumer)
    {
      super( channel);
      
      this.tag = tag;
      this.queue = queue;
      this.durable = durable;
      this.autoDelete = autoDelete;
      this.consumer = consumer;
    }
    
    /**
     * @return Returns the correct consumer tag.
     */
    public String getConsumerTag()
    {
      return tag;
    }
    
    /**
     * Clone this consumer, but assign a new channel.
     * @param channel The new channel.
     * @return Returns the clone.
     */
    public AutoRefreshConsumer cloneWithNewChannel( Channel channel) throws IOException
    {
      channel.queueDeclare( queue, durable, false, autoDelete, null);
      String tag = queue + counter.getAndIncrement();
      return new AutoRefreshConsumer( tag, queue, durable, autoDelete, channel, consumer);
    }
    
    /* (non-Javadoc)
     * @see com.rabbitmq.client.Consumer#handleDelivery(java.lang.String, com.rabbitmq.client.Envelope, com.rabbitmq.client.AMQP.BasicProperties, byte[])
     */
    @Override
    public void handleDelivery( String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException
    {
      try
      {
        consumer.handleDelivery( getChannel(), consumerTag, envelope, properties, body);
      }
      catch( Exception e)
      {
        log.warn( "Caught exception in consumer ...");
        log.exception( e);
      }
    }
    
    /* (non-Javadoc)
     * @see com.rabbitmq.client.DefaultConsumer#handleCancel(java.lang.String)
     */
    @Override
    public void handleCancel( String consumerTag) throws IOException
    {
    }

    /* (non-Javadoc)
     * @see com.rabbitmq.client.DefaultConsumer#handleCancelOk(java.lang.String)
     */
    @Override
    public void handleCancelOk( String consumerTag)
    {
    }

    /* (non-Javadoc)
     * @see com.rabbitmq.client.DefaultConsumer#handleConsumeOk(java.lang.String)
     */
    @Override
    public void handleConsumeOk( String consumerTag)
    {
    }

    /* (non-Javadoc)
     * @see com.rabbitmq.client.DefaultConsumer#handleRecoverOk(java.lang.String)
     */
    @Override
    public void handleRecoverOk( String consumerTag)
    {
    }

    /* (non-Javadoc)
     * @see com.rabbitmq.client.DefaultConsumer#handleShutdownSignal(java.lang.String, com.rabbitmq.client.ShutdownSignalException)
     */
    @Override
    public void handleShutdownSignal( String consumerTag, ShutdownSignalException signal)
    {
      Object reason = signal.getReason();
      if ( !(reason instanceof AMQP.Channel.Close) || ((AMQP.Channel.Close)reason).getReplyCode() != 200)
      {
        log.warnf( "handleShutdownSignal: %s, %s", consumerTag, signal);
      }
    }

    private String tag;
    private String queue;
    private boolean durable;
    private boolean autoDelete;
    private AmqpXioChannel consumer;
  }

  private final static Log log = Log.getLog( AutoRefreshConnection.class);
  
  private ConnectionFactory connectionFactory;
  private ExecutorService executor;
  private Address[] brokers;
  private Connection activeConnection;
  private Connection obsoleteConnection;
  private ThreadLocal<ThreadItem> threadItems;
  private List<ThreadItem> allThreadItems;
  private ReadWriteLock connectionLock;
  private Map<AmqpXioChannel, AutoRefreshConsumer> consumers;
  private BlockingQueue<Channel> obsoleteLeaseQueue;
  private ScheduledFuture<?> refreshSchedule;
  private AtomicInteger counter = new AtomicInteger();
}
