package org.xmodel.net.connection;

import java.util.ArrayList;
import java.util.List;

import org.xmodel.future.AsyncFuture;
import org.xmodel.log.SLog;

public abstract class AbstractNetworkConnection implements INetworkConnection
{
  public AbstractNetworkConnection( INetworkProtocol protocol)
  {
    this.protocol = protocol;
    listeners = new ArrayList<IListener>( 1);
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
  protected void onMessageReceived( Object message)
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
}
