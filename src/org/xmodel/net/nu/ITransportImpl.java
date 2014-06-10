package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledFuture;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

public interface ITransportImpl extends ITransport
{
  public ScheduledFuture<?> schedule( Runnable runnable, int delay);
  
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope);
  
  public Protocol getProtocol();
  
  public IContext getTransportContext();
  
  public boolean notifyReceive( byte[] bytes, int offset, int length) throws IOException;
  
  public boolean notifyReceive( ByteBuffer buffer) throws IOException;
  
  public void notifyReceive( IModelObject message, IContext messageContext, IModelObject request);
  
  public void notifyError( IContext context, ITransport.Error error, IModelObject request);
  
  public void notifyConnect();
  
  public void notifyDisconnect();
}
