package org.xmodel.net.transport.amqp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.xmodel.log.SLog;

import com.rabbitmq.client.AMQP.BasicProperties;
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
    this.consumers = Collections.synchronizedList( new ArrayList<AutoRefreshConsumer>());
    this.obsoleteLeaseQueue = new LinkedBlockingQueue<Channel>();
  }

  /**
   * Start the schedule for periodically refreshing the connection.
   * @param period The period in seconds.
   * @param scheduler The scheduler.
   */
  public void startRefreshSchedule( int period, ScheduledExecutorService scheduler)
  {
    Runnable refreshRunnable = new Runnable() {
      public void run()
      {
        refresh();
      }
    };
    
    refreshSchedule = scheduler.scheduleAtFixedRate( refreshRunnable, period, period, TimeUnit.SECONDS);
  }
  
  /**
   * Stop the current refresh schedule.
   */
  public void stopRefreshSchedule()
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
    long t0 = System.nanoTime();
    
    // create new connection
    Connection newConnection = createConnection();
    
    // stop consumers and close consumer channels
    for( AutoRefreshConsumer consumer: consumers)
    {
      try
      {
        Channel channel = consumer.getChannel();
        channel.flow( false);
        channel.close();
      }
      catch( IOException e)
      {
        SLog.warn( this, "Failed while shutting down consumers during refresh ...");
        SLog.exception( this, e);
        
        recoverAfterRefreshFailure();
      }
    }

    // swap connection under write lock
    int pendingLeaseCount = 0;
    long t1 = System.nanoTime();
    try
    {
      connectionLock.writeLock().lock();
      SLog.infof( this, "Write lock acquired in %1.2fms", (System.nanoTime() - t1) / 1e6);

      // swap
      obsoleteConnection = activeConnection;
      activeConnection = newConnection;
      
      // clear channel of all thread items
      for( ThreadItem item: allThreadItems.toArray( new ThreadItem[ 0]))
      {
        item.obsoleteChannel = item.channel;
        item.channel = null;
        if ( item.leased) pendingLeaseCount++; 
      }
    }
    finally
    {
      connectionLock.writeLock().unlock();
      SLog.infof( this, "Write lock held for %1.2fms", (System.nanoTime() - t1) / 1e6);
    }
    
    // restart consumers
    for( AutoRefreshConsumer consumer: consumers)
    {
      try
      {
        Channel channel = activeConnection.createChannel();
        
        AutoRefreshConsumer newConsumer = consumer.cloneWithNewChannel( channel);
        consumers.add( newConsumer);
        
        channel.basicQos( 1);
        channel.basicConsume( newConsumer.queue, false, newConsumer.queue, newConsumer);
      }
      catch( IOException e)
      {
        SLog.warn( this, "Lost connection to message bus while refreshing connection ...");
        SLog.exception( this, e);

        recoverAfterRefreshFailure();
      }
    }
    
    // wait for leased channels to be returned before closing old connection
    try
    {
      for( int i=0; i<pendingLeaseCount; i++)
        obsoleteLeaseQueue.take();
    }
    catch( InterruptedException e)
    {
      SLog.warn( this, "Interrupted while waiting for leases to be returned ...");
      SLog.exception( this, e);
    }
    
    // close old connection
    try
    {
      obsoleteConnection.close();
    }
    catch( IOException e)
    {
      SLog.warn( this, "Failed to close obsolete connection ...");
      SLog.exception( this, e);
    }
    
    SLog.infof( this, "Connection refresh took %1.3fs", (System.nanoTime() - t0) / 1e9);
  }
  
  /**
   * Recover after refresh failure.
   */
  private void recoverAfterRefreshFailure()
  {
    SLog.warn( this, "Recovering after refresh failure!");
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
    
      ThreadItem item = threadItems.get();
      if ( item == null || item.channel == null)
      {
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
        SLog.severe( this, "Incorrect thread channel returned.");
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
   * @param consumer The delegate consumer.
   */
  public void startConsumer( String queue, boolean durable, boolean autoDelete, AmqpXioChannel consumer) throws IOException
  {
    try
    {
      connectionLock.readLock().lock();
    
      Channel channel = activeConnection.createChannel();
      
      AutoRefreshConsumer refreshConsumer = new AutoRefreshConsumer( queue, durable, autoDelete, channel, consumer);
      consumers.add( refreshConsumer);
      
      //
      // Setting qos to 1 to insure that consumer does not have backlog of messages
      // that may be interrupted during the connection refresh flow.  The consumer
      // flow is stopped before the consumer channel is closed.
      //
      channel.basicQos( 1);
      channel.basicConsume( queue, false, queue, refreshConsumer);
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
        SLog.exception( this, e);
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
    throw new UnsupportedOperationException();
  }

  private static class ThreadItem
  {
    public Channel channel;
    public Channel obsoleteChannel;
    public boolean leased;
  }
  
  private class AutoRefreshConsumer extends DefaultConsumer
  {
    public AutoRefreshConsumer( String queue, boolean durable, boolean autoDelete, Channel channel, AmqpXioChannel consumer)
    {
      super( channel);
      
      this.queue = queue;
      this.durable = durable;
      this.autoDelete = autoDelete;
      this.consumer = consumer;
    }
    
    /**
     * Clone this consumer, but assign a new channel.
     * @param channel The new channel.
     * @return Returns the clone.
     */
    public AutoRefreshConsumer cloneWithNewChannel( Channel channel)
    {
      return new AutoRefreshConsumer( queue, durable, autoDelete, channel, consumer);
    }
    
    /* (non-Javadoc)
     * @see com.rabbitmq.client.Consumer#handleDelivery(java.lang.String, com.rabbitmq.client.Envelope, com.rabbitmq.client.AMQP.BasicProperties, byte[])
     */
    @Override
    public void handleDelivery( String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException
    {
      consumer.handleDelivery( getChannel(), consumerTag, envelope, properties, body);
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
    }

    private String queue;
    private boolean durable;
    private boolean autoDelete;
    private AmqpXioChannel consumer;
  }

  private ConnectionFactory connectionFactory;
  private ExecutorService executor;
  private Address[] brokers;
  private Connection activeConnection;
  private Connection obsoleteConnection;
  private ThreadLocal<ThreadItem> threadItems;
  private List<ThreadItem> allThreadItems;
  private ReadWriteLock connectionLock;
  private List<AutoRefreshConsumer> consumers;
  private BlockingQueue<Channel> obsoleteLeaseQueue;
  private ScheduledFuture<?> refreshSchedule;
}
