package org.xmodel.net.nu;

import java.util.concurrent.ScheduledFuture;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

public interface ITransportImpl extends ITransport
{
  public AsyncFuture<ITransport> send( IModelObject envelope, IContext messageContext, int timeout, int retries, int life);
  
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope, IModelObject request);
  
  public ScheduledFuture<?> schedule( Runnable runnable, int delay);
  
  public Protocol getProtocol();
  
  public IContext getTransportContext();
}
