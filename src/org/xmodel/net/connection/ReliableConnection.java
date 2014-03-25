package org.xmodel.net.connection;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.xmodel.future.AsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.log.SLog;

/**
 * This class provides a more reliable network connection by doing one or more of the following:
 * <ul>
 *   <li>Closing and re-connecting the underlying connection to refresh router tables.</li>
 *   <li>Automatically re-connecting when the underlying connection fails during a read or write operation.</li>
 * </ul> 
 */
public class ReliableConnection extends AbstractNetworkConnection
{
  public ReliableConnection( int lifetime, INetworkConnectionFactory connectionFactory)
  {
    this.lifetime = lifetime;
    this.connectionFactory = connectionFactory;
    this.connectionLock = new Object();
  }
    
  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#connect()
   */
  @Override
  public AsyncFuture<INetworkConnection> connect()
  {
    synchronized( connectionLock)
    {
      if ( activeConnection != null) return activeConnection.connect();
      activeConnection = connectionFactory.newConnection();
      activeConnection.addListener( consumer);
      scheduler.schedule( lifetimeExpiredTask, lifetime, TimeUnit.MILLISECONDS);
      return activeConnection.connect();
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#close()
   */
  @Override
  public AsyncFuture<INetworkConnection> closeImpl()
  {
    synchronized( connectionLock)
    {
      closeDyingConnection();
      if ( activeConnection != null) 
      {
        activeConnection.removeListener( consumer);
        return activeConnection.close();
      }
      return new SuccessAsyncFuture<INetworkConnection>( this);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#getCloseFuture()
   */
  @Override
  public AsyncFuture<INetworkConnection> getCloseFuture()
  {
    synchronized( connectionLock)
    {
      if ( activeConnection != null) return activeConnection.getCloseFuture();
      return new SuccessAsyncFuture<INetworkConnection>( this);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#send(org.xmodel.net.connection.INetworkMessage)
   */
  @Override
  public void send( INetworkMessage message) throws IOException
  {
    synchronized( connectionLock)
    {
      activeConnection.send( message);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.connection.AbstractNetworkConnection#request(org.xmodel.net.connection.INetworkMessage)
   */
  @Override
  public AsyncFuture<INetworkMessage> request( INetworkMessage request)
  {
    synchronized( connectionLock)
    {
      isActive = true;
      AsyncFuture<INetworkMessage> future = activeConnection.request( request);
      future.addListener( requestListener);
      return future;
    }
  }
  
  /**
   * Set whether this connection is currently handling a request/response transaction.  An active
   * connection will not be recycled until this method is called again with argument, false, indicating
   * that the transaction is complete.
   * @param active
   */
  public void setActive( boolean active)
  {
    synchronized( connectionLock)
    {
      isActive = active;
    }
  }

  /**
   * Create a new connection, but only update the active connection after the new connection is established.
   */
  private void expireActiveConnection()
  {
    synchronized( connectionLock)
    {
      try
      {
        INetworkConnection connection = connectionFactory.newConnection();
        connection.connect().addListener( reconnectListener);
      }
      catch( Exception e)
      {
        SLog.exception( this, e);
      }
    }    
  }
  
  /**
   * Set the new active connection.
   * @param connection The new connection.
   */
  private void newActiveConnection( INetworkConnection connection)
  {
    closeDyingConnection();

    INetworkConnection connectionToClose = null;
    
    synchronized( connectionLock)
    {
      dyingConnection = activeConnection;
      activeConnection = connection;
      activeConnection.addListener( consumer);
          
      if ( !isActive)
      {
        connectionToClose = dyingConnection;
        dyingConnection = null;
      }
    }
    
    if ( connectionToClose != null)
    {
      connectionToClose.removeListener( consumer);
      connectionToClose.close();
    }
  }
  
  /**
   * Close the dying connection if present.
   */
  private void closeDyingConnection()
  {
    INetworkConnection connection = null;
    
    synchronized( connectionLock)
    {
      connection = dyingConnection;
      dyingConnection = null;
    }
    
    if ( connection != null) 
    {
      connection.removeListener( consumer);
      connection.close();
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.connection.AbstractNetworkConnection#handleListenerException(java.lang.Exception)
   */
  @Override
  protected void handleListenerException( Exception e)
  {
    SLog.exception( this, e);
  }

  private Runnable lifetimeExpiredTask = new Runnable() {
    public void run()
    {
      expireActiveConnection();
    }
  };
  
  private AsyncFuture.IListener<INetworkMessage> requestListener = new AsyncFuture.IListener<INetworkMessage>() {
    public void notifyComplete( AsyncFuture<INetworkMessage> future) throws Exception
    {
      synchronized( connectionLock) { isActive = false;}
      
      closeDyingConnection();
    }
  };
  
  private AsyncFuture.IListener<INetworkConnection> reconnectListener = new AsyncFuture.IListener<INetworkConnection>() {
    public void notifyComplete( AsyncFuture<INetworkConnection> future) throws Exception
    {
      newActiveConnection( future.getInitiator());
    }
  };
  
  private INetworkConnection.IListener consumer = new INetworkConnection.IListener() {
    public void onMessageReceived( INetworkConnection connection, INetworkMessage message)
    {
      ReliableConnection.this.onMessageReceived( message);
    }
    public void onClose( INetworkConnection connection, Object cause)
    {
      ReliableConnection.this.onClose( cause);
    }
  };

  private INetworkConnectionFactory connectionFactory;
  private INetworkConnection activeConnection;
  private INetworkConnection dyingConnection;
  private Object connectionLock;
  private boolean isActive;
  private int lifetime;
  private ScheduledExecutorService scheduler;
}