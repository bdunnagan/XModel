package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.xmodel.IModelObject;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.xpath.expression.IContext;

public class DefaultEventHandler implements IEventHandler
{
  @Override
  public boolean notifyReceive( ByteBuffer buffer) throws IOException
  {
    return false;
  }

  @Override
  public boolean notifyReceive( IModelObject envelope)
  {
    return false;
  }

  @Override
  public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage)
  {
    return false;
  }

  @Override
  public boolean notifyConnect( IContext transportContext) throws IOException
  {
    return false;
  }

  @Override
  public boolean notifyDisconnect( IContext transportContext) throws IOException
  {
    return false;
  }

  @Override
  public boolean notifyRegister( IContext transportContext, String name)
  {
    return false;
  }

  @Override
  public boolean notifyDeregister( IContext transportContext, String name)
  {
    return false;
  }

  @Override
  public boolean notifyError( IContext context, Error error, IModelObject request)
  {
    return false;
  }

  @Override
  public boolean notifyException( IOException e)
  {
    return false;
  }
}
