package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;

public interface IEventHandler
{
  public void notifyReceive( ByteBuffer buffer) throws IOException;

  public void notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage);
  
  public void notifyConnect() throws IOException;
  
  public void notifyDisconnect() throws IOException;
  
  public void notifyError( IContext context, ITransport.Error error, IModelObject request);

  public void notifyException( IOException e);
}
