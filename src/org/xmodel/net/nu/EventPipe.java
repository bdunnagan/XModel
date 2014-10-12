package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import org.xmodel.IModelObject;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.xpath.expression.IContext;

public class EventPipe implements IEventHandler
{
  public EventPipe()
  {
    handlers = new ArrayDeque<IEventHandler>( 3);
  }
  
  public synchronized void addFirst( IEventHandler handler)
  {
    handlers.addFirst( handler);
  }
  
  public synchronized void addLast( IEventHandler handler)
  {
    handlers.addLast( handler);
  }
  
  public synchronized void remove( IEventHandler handler)
  {
    handlers.remove( handler);
  }
  
  private synchronized IEventHandler[] getHandlers()
  {
    return handlers.toArray( new IEventHandler[ 0]);
  }
  
  @Override
  public boolean notifySend( ITransportImpl transport, IModelObject envelope, IContext messageContext, int timeout, int retries, int life) throws IOException
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifySend( transport, envelope, messageContext, timeout, retries, life))
        return true;
    }
    return false;
  }

  @Override
  public boolean notifyReceive( ITransportImpl transport, ByteBuffer buffer) throws IOException
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifyReceive( transport, buffer))
        return true;
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.IEventHandler#notifyReceive(org.xmodel.IModelObject)
   */
  @Override
  public boolean notifyReceive( ITransportImpl transport, IModelObject envelope)
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifyReceive( transport, envelope))
        return true;
    }
    return false;
  }

  @Override
  public boolean notifyReceive( ITransportImpl transport, IModelObject message, IContext messageContext, IModelObject requestMessage)
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifyReceive( transport, message, messageContext, requestMessage))
        return true;
    }
    return false;
  }

  @Override
  public boolean notifyConnect( ITransportImpl transport, IContext transportContext) throws IOException
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifyConnect( transport, transportContext))
        return true;
    }
    return false;
  }

  @Override
  public boolean notifyDisconnect(ITransportImpl transport, IContext transportContext) throws IOException
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifyDisconnect( transport, transportContext))
        return true;
    }
    return false;
  }

  @Override
  public boolean notifyRegister( ITransportImpl transport, IContext transportContext, String name)
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifyRegister( transport, transportContext, name))
        return true;
    }
    return false;
  }

  @Override
  public boolean notifyDeregister( ITransportImpl transport, IContext transportContext, String name)
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifyDeregister( transport, transportContext, name))
        return true;
    }
    return false;
  }

  @Override
  public boolean notifyError( ITransportImpl transport, IContext context, Error error, IModelObject request)
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifyError( transport, context, error, request))
        return true;
    }
    return false;
  }

  @Override
  public boolean notifyException( ITransportImpl transport, IOException e)
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifyException( transport, e))
        return true;
    }
    return false;
  }

  private Deque<IEventHandler> handlers;
}
