package org.xmodel.net.connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmodel.future.AsyncFuture;
import org.xmodel.log.SLog;

public abstract class AbstractNetworkConnection implements INetworkConnection
{
  public AbstractNetworkConnection( INetworkProtocol protocol)
  {
    this.protocol = protocol;
    listeners = new ArrayList<IListener>( 1);
    requestFutures = new HashMap<Object, RequestFuture>();
    requestFuturesLock = new Object();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#getProtocol()
   */
  @Override
  public INetworkProtocol getProtocol()
  {
    return protocol;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkConnection#request(org.xmodel.net.connection.INetworkMessage, int)
   */
  @Override
  public RequestFuture request( final Object request, Object correlation)
  {
    RequestFuture requestFuture = new RequestFuture( this, request, correlation);
    
    putRequestFuture( correlation, requestFuture);
    
    try
    {
      send( request);
    }
    catch( Exception e)
    {
      removeRequestFuture( correlation);
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
   * @param correlation The correlation key.
   */
  protected void onMessageReceived( Object message, Object correlation)
  {
    try
    {
      // correlate the response
      RequestFuture requestFuture = getRequestFuture( correlation);
      if ( requestFuture != null) requestFuture.setResponse( message);
      
      // notify listeners
      for( IListener listener: getListeners())
      {
        try
        {
          listener.onMessageReceived( this, message, correlation);
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
      if ( requestFuture != null) requestFuture.notifySuccess();
    }
    finally
    {
      // cleanup
      removeRequestFuture( correlation);
    }
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
    for( AsyncFuture<Object> requestFuture: removeAllRequestFutures())
    {
      requestFuture.notifyFailure( closedFutureMessage);
    }
  }
  
  /**
   * Save a request future by the specified correlation object.
   * @param key The correlation object.
   * @param requestFuture The request future.
   */
  private void putRequestFuture( Object key, RequestFuture requestFuture)
  {
    synchronized( requestFuturesLock)
    {
      requestFutures.put( key, requestFuture);
    }
  }
  
  /**
   * Get the request future for the specified correlation object.
   * @param key The correlation object.
   * @return Returns null or the request future.
   */
  private RequestFuture getRequestFuture( Object key)
  {
    synchronized( requestFuturesLock)
    {
      return requestFutures.get( key);
    }    
  }
  
  /**
   * Remove the request future for the specified correlation object.
   * @param key The correlation object.
   * @return Returns null or the request future.
   */
  protected RequestFuture removeRequestFuture( Object key)
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
  private List<RequestFuture> removeAllRequestFutures()
  {
    synchronized( requestFuturesLock)
    {
      List<RequestFuture> list = new ArrayList<RequestFuture>();
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
  
  INetworkProtocol protocol;  
  private List<IListener> listeners;
  private Map<Object, RequestFuture> requestFutures;
  private Object requestFuturesLock;
}
