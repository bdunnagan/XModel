package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.xpath.expression.IContext;

public class EventHandlerChain implements IEventHandler
{
  public EventHandlerChain()
  {
    deque = new ArrayDeque<IEventHandler>();
  }

  @Override
  public boolean notifyReceive( ByteBuffer buffer) throws IOException
  {
    return false;
  }

  @Override
  public boolean notifyConnect() throws IOException
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean notifyDisconnect() throws IOException
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean notifyError( IContext context, Error error, IModelObject request) throws Exception
  {
    // TODO Auto-generated method stub
    return false;
  }

  private Deque<IEventHandler> deque;
}
