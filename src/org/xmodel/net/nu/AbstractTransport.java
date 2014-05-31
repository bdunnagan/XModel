package org.xmodel.net.nu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.xpath.expression.IContext;

public abstract class AbstractTransport implements ITransport
{
  protected AbstractTransport( IProtocol protocol, IContext transportContext, ScheduledExecutorService scheduler)
  {
    this.protocol = protocol;
    this.transportContext = transportContext;
    this.scheduler = scheduler;
    this.requestCounter = new AtomicLong( System.nanoTime() & 0x7FFFFFFFFFFFFFFFL);
    this.receiveListeners = new ArrayList<IReceiveListener>( 1);
    this.timeoutListeners = new ArrayList<ITimeoutListener>( 1);
    this.connectListeners = new ArrayList<IConnectListener>( 1);
    this.disconnectListeners = new ArrayList<IDisconnectListener>( 1);
  }
  
  @Override
  public final AsyncFuture<ITransport> send( IModelObject message, IContext messageContext, int timeout) throws IOException
  {
    String key = Long.toHexString( requestCounter.incrementAndGet());
    message.setAttribute( "id", key);
    
    Request request = new Request( message, messageContext, timeout);
    requests.put( key, request);
    
    return send( message);
  }
    
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
  public void addListener( ITimeoutListener listener)
  {
    if ( !timeoutListeners.contains( listener))
      timeoutListeners.add( listener);
  }

  @Override
  public void removeListener( ITimeoutListener listener)
  {
    timeoutListeners.remove( listener);
  }

  @Override
  public void addListener( IConnectListener listener)
  {
    if ( !connectListeners.contains( listener))
      connectListeners.add( listener);
  }

  @Override
  public void removeListener( IConnectListener listener)
  {
    connectListeners.remove( listener);
  }

  @Override
  public void addListener( IDisconnectListener listener)
  {
    if ( !disconnectListeners.contains( listener))
      disconnectListeners.add( listener);
  }

  @Override
  public void removeListener( IDisconnectListener listener)
  {
    disconnectListeners.remove( listener);
  }
  
  public void notifyReceive( byte[] bytes, int offset, int length)
  {
    // decode
    IModelObject message = protocol.decode( bytes, offset, length);

    // release request
    Request request = requests.remove( message.getAttribute( "id"));
    
    IModelObject requestMessage = null;
    IContext messageContext = null;
    if ( request != null)
    {
      requestMessage = request.message;
      messageContext = request.messageContext;
    }
    
    // receive/timeout exclusion
    if ( request.timeoutFuture.cancel( false))
    {
      // notify listeners
      IReceiveListener[] listeners = receiveListeners.toArray( new IReceiveListener[ 0]);
      for( IReceiveListener listener: listeners)
      {
        try
        {
          listener.onReceive( this, message, messageContext, requestMessage);
        }
        catch( Exception e)
        {
          log.exception( e);
        }
      }
    }
  }
  
  public void notifyTimeout( IModelObject message, IContext messageContext)
  {
    // release request
    requests.remove( message.getAttribute( "id"));
    
    // notify listeners
    ITimeoutListener[] listeners = timeoutListeners.toArray( new ITimeoutListener[ 0]);
    for( ITimeoutListener listener: listeners)
    {
      try
      {
        listener.onTimeout( this, message, messageContext);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }
  
  public void notifyConnect()
  {
    // notify listeners
    IConnectListener[] listeners = connectListeners.toArray( new IConnectListener[ 0]);
    for( IConnectListener listener: listeners)
    {
      try
      {
        listener.onConnect( this, transportContext);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }
  
  public void notifyDisconnect()
  {
    // notify listeners
    IDisconnectListener[] listeners = disconnectListeners.toArray( new IDisconnectListener[ 0]);
    for( IDisconnectListener listener: listeners)
    {
      try
      {
        listener.onDisconnect( this, transportContext);
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
  
  private class Request implements Runnable
  {
    Request( IModelObject message, IContext messageContext, int timeout)
    {
      this.message = message;
      this.messageContext = messageContext;
      this.timeoutFuture = scheduler.schedule( this, timeout, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void run()
    {
      notifyTimeout( message, messageContext);
    }
    
    IModelObject message;
    IContext messageContext;
    ScheduledFuture<?> timeoutFuture;
  }
  
  public final static Log log = Log.getLog( AbstractTransport.class);
  
  private IProtocol protocol;
  private IContext transportContext;
  private ScheduledExecutorService scheduler;
  private Map<String, Request> requests;
  private AtomicLong requestCounter;
  private List<IReceiveListener> receiveListeners;
  private List<ITimeoutListener> timeoutListeners;
  private List<IConnectListener> connectListeners;
  private List<IDisconnectListener> disconnectListeners;
}
