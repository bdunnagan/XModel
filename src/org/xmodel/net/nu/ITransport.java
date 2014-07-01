package org.xmodel.net.nu;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.xpath.expression.IContext;

/**
 * (thread-safe)
 */
public interface ITransport
{
  public enum Error { timeout, connectRefused, connectError, encodeFailed, channelClosed, messageExpired};

  public AsyncFuture<ITransport> connect( int timeout);
  
  public AsyncFuture<ITransport> disconnect();

  public AsyncFuture<ITransport> request( IModelObject message, IContext messageContext, int timeout);
  
  public AsyncFuture<ITransport> ack( IModelObject request);
  
  public AsyncFuture<ITransport> respond( IModelObject message, IModelObject request);
  
  public TransportNotifier getNotifier();
}
