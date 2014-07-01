package org.xmodel.net.nu;

import java.util.concurrent.ScheduledFuture;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

public interface ITransportImpl extends ITransport, IEventHandler
{
  public void setEventHandler( IEventHandler handler);
  
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope);
  
  public ScheduledFuture<?> schedule( Runnable runnable, int delay);
  
  public Protocol getProtocol();
  
  public IContext getTransportContext();
}
