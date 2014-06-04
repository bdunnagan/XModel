package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.future.AsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.util.PrefixThreadFactory;
import org.xmodel.xpath.expression.IContext;

public abstract class AbstractTransport implements ITransport
{
  protected AbstractTransport( IProtocol protocol, IContext transportContext, ScheduledExecutorService scheduler, 
     List<IReceiveListener> receiveListeners, List<ITimeoutListener> timeoutListeners, 
     List<IConnectListener> connectListeners, List<IDisconnectListener> disconnectListeners)
  {
    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1, new PrefixThreadFactory( "scheduler"));
    if ( receiveListeners == null) receiveListeners = Collections.emptyList(); 
    if ( timeoutListeners == null) timeoutListeners = Collections.emptyList(); 
    if ( connectListeners == null) connectListeners = Collections.emptyList(); 
    if ( disconnectListeners == null) disconnectListeners = Collections.emptyList(); 
    
    this.protocol = new ThreadSafeProtocol( protocol);
    this.transportContext = transportContext;
    this.scheduler = scheduler;
    this.requests = new ConcurrentHashMap<String, Request>();
    this.requestCounter = new AtomicLong( System.nanoTime() & 0x7FFFFFFFFFFFFFFFL);
    
    this.receiveListeners = new CopyOnWriteArrayList<IReceiveListener>( receiveListeners);
    this.timeoutListeners = new CopyOnWriteArrayList<ITimeoutListener>( timeoutListeners);
    this.connectListeners = new CopyOnWriteArrayList<IConnectListener>( connectListeners);
    this.disconnectListeners = new CopyOnWriteArrayList<IDisconnectListener>( disconnectListeners);
  }
  
  @Override
  public final AsyncFuture<ITransport> send( IModelObject message, IContext messageContext, int timeout) throws IOException
  {
    String key = Long.toHexString( requestCounter.incrementAndGet());
    message.setAttribute( "id", key);
    
    Request request = new Request( message, messageContext, timeout);
    requests.put( key, request);
    System.out.println( key);
    
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
  
  public boolean notifyReceive( byte[] bytes, int offset, int length) throws IOException
  {
    try
    {
      // decode
      IModelObject message = protocol.decode( bytes, offset, length);
      if ( message == null) return false;
      
      // deliver
      return notifyReceive( message);
    }
    catch( Exception e)
    {
      log.exception( e);
      return false;
    }
  }
  
  public boolean notifyReceive( ByteBuffer buffer) throws IOException
  {
    try
    {
      // decode
      IModelObject message = protocol.decode( buffer);
      if ( message == null) return false;
      
      // deliver
      return notifyReceive( message);
    }
    catch( Exception e)
    {
      log.exception( e);
      return false;
    }
  }
  
  private boolean notifyReceive( IModelObject message) throws IOException
  {
    // get route
    String route = Xlate.get( message, "route", (String)null);
    
    // lookup request and free
    Object id = message.getAttribute( "id");
    Request request = (id != null)? requests.remove( id): null;
    if ( request != null)
    {
      // receive/timeout exclusion
      if ( request.timeoutFuture.cancel( false))
      {
        for( IReceiveListener listener: receiveListeners)
        {
          try
          {
            listener.onReceive( this, message, request.messageContext, request.message);
          }
          catch( Exception e)
          {
            log.exception( e);
          }
        }
      }
    }
    else if ( route == null)
    {
      for( IReceiveListener listener: receiveListeners)
      {
        try
        {
          listener.onReceive( this, message, transportContext, null);
        }
        catch( Exception e)
        {
          log.exception( e);
        }
      }
    }
    else
    {
      // TODO
      throw new UnsupportedOperationException();
    }
    
    return true;
  }
  
  public void notifyTimeout( IModelObject message, IContext messageContext)
  {
    // release request
    requests.remove( message.getAttribute( "id"));
    
    // notify listeners
    for( ITimeoutListener listener: timeoutListeners)
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
    for( IConnectListener listener: connectListeners)
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
    for( IDisconnectListener listener: disconnectListeners)
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
