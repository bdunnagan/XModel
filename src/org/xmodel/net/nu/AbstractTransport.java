package org.xmodel.net.nu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.xpath.expression.IContext;

public abstract class AbstractTransport implements ITransport
{
  protected AbstractTransport( IProtocol protocol, IContextManager contexts)
  {
    this.protocol = protocol;
    this.contexts = contexts;
    this.receiveListeners = new ArrayList<IReceiveListener>( 1);
    this.connectListeners = new ArrayList<IConnectListener>( 1);
    this.disconnectListeners = new ArrayList<IDisconnectListener>( 1);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.ITransport#getContexts()
   */
  @Override
  public IContextManager getContexts()
  {
    return contexts;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.ITransport#send(org.xmodel.IModelObject, int)
   */
  @Override
  public final AsyncFuture<ITransport> send( IModelObject message, int timeout) throws IOException
  {
    contexts.beginMessageContext( message);
    return sendImpl( message, timeout);
  }
  
  protected abstract AsyncFuture<ITransport> sendImpl( IModelObject message, int timeout) throws IOException;
  
  @Override
  public void addListener( IReceiveListener listener)
  {
    if ( !receiveListeners.contains( listener))
      receiveListeners.add( listener);
  }

  @Override
  public void removeListener( IReceiveListener listener)
  {
    receiveListeners.remove( listener);
  }

  @Override
  public void addConnectListener( IConnectListener listener)
  {
    if ( !connectListeners.contains( listener))
      connectListeners.add( listener);
  }

  @Override
  public void removeConnectListener( IConnectListener listener)
  {
    connectListeners.remove( listener);
  }

  @Override
  public void addDisconnectListener( IDisconnectListener listener)
  {
    if ( !disconnectListeners.contains( listener))
      disconnectListeners.add( listener);
  }

  @Override
  public void removeDisconnectListener( IDisconnectListener listener)
  {
    disconnectListeners.remove( listener);
  }
  
  public void notifyReceive( byte[] bytes, int offset, int length)
  {
    // decode
    IModelObject message = protocol.decode( bytes, offset, length);

    // release message context
    IContext context = contexts.endMessageContext( message);
    
    // notify listeners
    IReceiveListener[] listeners = receiveListeners.toArray( new IReceiveListener[ 0]);
    for( IReceiveListener listener: listeners)
    {
      try
      {
        listener.onReceive( this, message, context);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }
  
  public void notifyConnect()
  {
    // get transport context
    IContext context = contexts.getTransportContext();
    
    // notify listeners
    IConnectListener[] listeners = connectListeners.toArray( new IConnectListener[ 0]);
    for( IConnectListener listener: listeners)
    {
      try
      {
        listener.onConnect( this, context);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }
  
  public void notifyDisconnect()
  {
    // get transport context
    IContext context = contexts.getTransportContext();
    
    // notify listeners
    IDisconnectListener[] listeners = disconnectListeners.toArray( new IDisconnectListener[ 0]);
    for( IDisconnectListener listener: listeners)
    {
      try
      {
        listener.onDisconnect( this, context);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }

  protected IProtocol getProtocol()
  {
    return protocol;
  }
  
  public final static Log log = Log.getLog( AbstractTransport.class);
  
  private IProtocol protocol;
  private IContextManager contexts;
  private List<IReceiveListener> receiveListeners;
  private List<IConnectListener> connectListeners;
  private List<IDisconnectListener> disconnectListeners;
}
