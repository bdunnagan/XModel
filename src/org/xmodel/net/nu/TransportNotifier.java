package org.xmodel.net.nu;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.xpath.expression.IContext;

public class TransportNotifier
{
  public TransportNotifier()
  {
    connectListeners = new CopyOnWriteArrayList<IConnectListener>();
    disconnectListeners = new CopyOnWriteArrayList<IDisconnectListener>();
    receiveListeners = new CopyOnWriteArrayList<IReceiveListener>();
    errorListeners = new CopyOnWriteArrayList<IErrorListener>();
  }

  public void setConnectListeners( List<IConnectListener> listeners)
  {
    connectListeners = new CopyOnWriteArrayList<IConnectListener>( listeners);
  }

  public void setDisconnectListeners( List<IDisconnectListener> listeners)
  {
    disconnectListeners = new CopyOnWriteArrayList<IDisconnectListener>( listeners);
  }

  public void setReceiveListeners( List<IReceiveListener> listeners)
  {
    receiveListeners = new CopyOnWriteArrayList<IReceiveListener>( listeners);
  }

  public void setErrorListeners( List<IErrorListener> listeners)
  {
    errorListeners = new CopyOnWriteArrayList<IErrorListener>( listeners);
  }

  public void addListener( IConnectListener listener)
  {
    if ( !connectListeners.contains( listener))
      connectListeners.add( listener);
  }

  public void removeListener( IConnectListener listener)
  {
    connectListeners.remove( listener);
  }

  public void addListener( IDisconnectListener listener)
  {
    if ( !disconnectListeners.contains( listener))
      disconnectListeners.add( listener);
  }

  public void removeListener( IDisconnectListener listener)
  {
    disconnectListeners.remove( listener);
  }
  
  public void addListener( IReceiveListener listener)
  {
    if ( !receiveListeners.contains( listener))
      receiveListeners.add( listener);
  }

  public void removeListener( IReceiveListener listener)
  {
    receiveListeners.remove( listener);
  }

  public void addListener( IErrorListener listener)
  {
    if ( !errorListeners.contains( listener))
      errorListeners.add( listener);
  }

  public void removeListener( IErrorListener listener)
  {
    errorListeners.remove( listener);
  }

  public void notifyReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject request)
  {
    for( IReceiveListener listener: receiveListeners)
    {
      try
      {
        listener.onReceive( transport, message, messageContext, request);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }
  
  public void notifyError( ITransport transport, IContext context, ITransport.Error error, IModelObject request)
  {
    for( IErrorListener listener: errorListeners)
    {
      try
      {
        listener.onError( transport, context, error, request);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }
  
  public void notifyConnect( ITransport transport, IContext transportContext)
  {
    for( IConnectListener listener: connectListeners)
    {
      try
      {
        listener.onConnect( transport, transportContext);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }
  
  public void notifyDisconnect( ITransport transport, IContext transportContext)
  {
    for( IDisconnectListener listener: disconnectListeners)
    {
      try
      {
        listener.onDisconnect( transport, transportContext);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }
  
  public final static Log log = Log.getLog( TransportNotifier.class);
  
  private List<IConnectListener> connectListeners;
  private List<IDisconnectListener> disconnectListeners;
  private List<IReceiveListener> receiveListeners;
  private List<IErrorListener> errorListeners;
}
