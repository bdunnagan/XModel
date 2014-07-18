package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;

public interface IEventHandler
{
  public boolean notifyReceive( ByteBuffer buffer) throws IOException;

  public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage);
  
  public boolean notifyConnect( IContext transportContext) throws IOException;
  
  public boolean notifyDisconnect( IContext transportContext) throws IOException;
  
  public boolean notifyError( IContext context, ITransport.Error error, IModelObject request);

  public boolean notifyException( IOException e);
}
