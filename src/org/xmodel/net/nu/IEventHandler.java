package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;

public interface IEventHandler
{
  public void setNextEventHandler( IEventHandler next);
  
  public boolean notifyReceive( byte[] bytes, int offset, int length) throws IOException;
  
  public boolean notifyReceive( ByteBuffer buffer) throws IOException;
  
  public void notifyConnect() throws IOException;
  
  public void notifyDisconnect() throws IOException;
  
  public void notifyError( IContext context, ITransport.Error error, IModelObject request) throws Exception;
}
