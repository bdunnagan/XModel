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
  public boolean notifyReceive( ByteBuffer buffer) throws IOException
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifyReceive( buffer))
        return true;
    }
    return false;
  }

  @Override
  public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage)
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifyReceive( message, messageContext, requestMessage))
        return true;
    }
    return false;
  }

  @Override
  public boolean notifyConnect(IContext transportContext) throws IOException
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifyConnect(null))
        return true;
    }
    return false;
  }

  @Override
  public boolean notifyDisconnect(IContext transportContext) throws IOException
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifyDisconnect(null))
        return true;
    }
    return false;
  }

  @Override
  public boolean notifyError( IContext context, Error error, IModelObject request)
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifyError( context, error, request))
        return true;
    }
    return false;
  }

  @Override
  public boolean notifyException( IOException e)
  {
    for( IEventHandler handler: getHandlers())
    {
      if ( handler.notifyException( e))
        return true;
    }
    return false;
  }

  private Deque<IEventHandler> handlers;
}
