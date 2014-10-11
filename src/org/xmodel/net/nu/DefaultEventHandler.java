package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.xmodel.IModelObject;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.xpath.expression.IContext;

public class DefaultEventHandler implements IEventHandler
{
  @Override
  public boolean notifySend( ITransportImpl transport, IModelObject envelope, IContext messageContext, int timeout, int retries, int life)
  {
    return false;
  }

  @Override
  public boolean notifyReceive( ITransportImpl transport, ByteBuffer buffer) throws IOException
  {
    return false;
  }

  @Override
  public boolean notifyReceive( ITransportImpl transport, IModelObject envelope)
  {
    return false;
  }

  @Override
  public boolean notifyReceive( ITransportImpl transport, IModelObject message, IContext messageContext, IModelObject requestMessage)
  {
    return false;
  }

  @Override
  public boolean notifyConnect( ITransportImpl transport, IContext transportContext) throws IOException
  {
    return false;
  }

  @Override
  public boolean notifyDisconnect( ITransportImpl transport, IContext transportContext) throws IOException
  {
    return false;
  }

  @Override
  public boolean notifyRegister( ITransportImpl transport, IContext transportContext, String name)
  {
    return false;
  }

  @Override
  public boolean notifyDeregister( ITransportImpl transport, IContext transportContext, String name)
  {
    return false;
  }

  @Override
  public boolean notifyError( ITransportImpl transport, IContext context, Error error, IModelObject request)
  {
    return false;
  }

  @Override
  public boolean notifyException( ITransportImpl transport, IOException e)
  {
    return false;
  }
}
