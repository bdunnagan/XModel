package org.xmodel.net.connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmodel.future.AsyncFuture;
import org.xmodel.log.SLog;

public abstract class AbstractNetworkConnection implements INetworkConnection
{
  public AbstractNetworkConnection()
  {
    listeners = new ArrayList<IListener>( 1);
    requestFutures = new HashMap<Object, AsyncFuture<INetworkMessage>>();
    requestFuturesLock = new Object();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#request(org.xmodel.net.connection.INetworkMessage, int)
   */
  @Override
  public AsyncFuture<INetworkMessage> request( INetworkMessage request, int timeout)
  {
    AsyncFuture<INetworkMessage> requestFuture = new AsyncFuture<INetworkMessage>( request);
    
    Object key = request.getCorrelation();
    putRequestFuture( key, requestFuture);
    
    try
    {
      send( request);
    }
    catch( Exception e)
    {
      removeRequestFuture( key);
      requestFuture.notifyFailure( e);
    }
    
    return requestFuture;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#close()
   */
  @Override
  public final AsyncFuture<INetworkConnection> close()
  {
    AsyncFuture<INetworkConnection> closeFuture = closeImpl();
    closeFuture.addListener( closeListener);
    return closeFuture;
  }
  
  /**
   * Sub-class implementation of close method.
   * @return Returns the future for the operation.
   */
  protected abstract AsyncFuture<INetworkConnection> closeImpl();

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#addListener(org.xmodel.net.connection.INetworkConnection.IListener)
   */
  @Override
  public void addListener( IListener listener)
  {
    synchronized( listeners)
    {
      listeners.add( listener);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#removeListener(org.xmodel.net.connection.INetworkConnection.IListener)
   */
  @Override
  public void removeListener( IListener listener)
  {
    synchronized( listeners)
    {
      listeners.remove( listener);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#getListeners()
   */
  @Override
  public IListener[] getListeners()
  {
    synchronized( listeners)
    {
      return listeners.toArray( new IListener[ 0]);
    }
  }

  /**
   * Sub-classes must call this method when a message is received.
   * @param message The message.
   */
  protected void onMessageReceived( INetworkMessage message)
  {
    // notify listeners
    for( IListener listener: getListeners())
    {
      try
      {
        listener.onMessageReceived( this, message);
      }
      catch( Exception e)
      {
        try
        {
          handleListenerException( e);
        }
        catch( Exception e2)
        {
          SLog.exception( this, e2);
        }
      }
    }
    
    // notify request future
    AsyncFuture<INetworkMessage> requestFuture = removeRequestFuture( message.getCorrelation());
    if ( requestFuture != null) requestFuture.notifySuccess();
  }
  
  /**
   * Sub-classes must call this method when the connection is closed either explicitly or unexpectedly.
   * @param cause See INetworkConnection.IListener for explanation of causes.
   */
  protected void onClose( Object cause)
  {
    // notify listeners
    for( IListener listener: getListeners())
    {
      try
      {
        listener.onClose( this, cause);
      }
      catch( Exception e)
      {
        try
        {
          handleListenerException( e);
        }
        catch( Exception e2)
        {
          SLog.exception( this, e2);
        }
      }
    }
    
    // notify request futures
    for( AsyncFuture<INetworkMessage> requestFuture: removeAllRequestFutures())
    {
      requestFuture.notifyFailure( closedFutureMessage);
    }
  }
  
  /**
   * Save a request future by the specified correlation object.
   * @param key The correlation object.
   * @param requestFuture The request future.
   */
  private void putRequestFuture( Object key, AsyncFuture<INetworkMessage> requestFuture)
  {
    synchronized( requestFuturesLock)
    {
      requestFutures.put( key, requestFuture);
    }
  }
  
  /**
   * Remove the request future for the specified correlation object.
   * @param key The correlation object.
   * @return Returns null or the request future.
   */
  private AsyncFuture<INetworkMessage> removeRequestFuture( Object key)
  {
    synchronized( requestFuturesLock)
    {
      return requestFutures.remove( key);
    }    
  }
  
  /**
   * Remove and return all request futures.
   * @return Returns the list of request futures.
   */
  private List<AsyncFuture<INetworkMessage>> removeAllRequestFutures()
  {
    synchronized( requestFuturesLock)
    {
      List<AsyncFuture<INetworkMessage>> list = new ArrayList<AsyncFuture<INetworkMessage>>();
      list.addAll( requestFutures.values());
      requestFutures.clear();
      return list;
    }    
  }
  
  /**
   * Called when an exception is thrown by a listener.
   * @param e The exception.
   */
  protected abstract void handleListenerException( Exception e);
  
  private AsyncFuture.IListener<INetworkConnection> closeListener = new AsyncFuture.IListener<INetworkConnection>() {
    public void notifyComplete( AsyncFuture<INetworkConnection> future) throws Exception
    {
      onClose( closedFutureMessage);
    }
  };
  
  private List<IListener> listeners;
  private Map<Object, AsyncFuture<INetworkMessage>> requestFutures;
  private Object requestFuturesLock;
}
