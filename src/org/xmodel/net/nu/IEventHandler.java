package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;

public interface IEventHandler
{
  public boolean notifySend( ITransportImpl transport, IModelObject envelope, IContext messageContext, int timeout, int retries, int life);
  
  public boolean notifyReceive( ITransportImpl transport, ByteBuffer buffer) throws IOException;
  
  public boolean notifyReceive( ITransportImpl transport, IModelObject envelope);

  public boolean notifyReceive( ITransportImpl transport, IModelObject message, IContext messageContext, IModelObject request);
  
  public boolean notifyConnect( ITransportImpl transport, IContext transportContext) throws IOException;
  
  public boolean notifyDisconnect( ITransportImpl transport, IContext transportContext) throws IOException;
  
  public boolean notifyRegister( ITransportImpl transport, IContext transportContext, String name);
  
  public boolean notifyDeregister( ITransportImpl transport, IContext transportContext, String name);
  
  public boolean notifyError( ITransportImpl transport, IContext context, ITransport.Error error, IModelObject request);

  public boolean notifyException( ITransportImpl transport, IOException e);
}
