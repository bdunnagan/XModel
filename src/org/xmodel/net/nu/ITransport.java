package org.xmodel.net.nu;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

/**
 * (thread-safe)
 */
public interface ITransport
{
  public enum Error { timeout, connectRefused, connectError, encodeFailed, sendFailed, channelClosed, messageExpired, heartbeatLost};

  public void setConnectTimeout( int timeout);
  
  public AsyncFuture<ITransport> connect();
  
  public AsyncFuture<ITransport> disconnect( boolean reconnect);
  
  public AsyncFuture<ITransport> send( IModelObject request, IModelObject envelope, IContext messageContext, int timeout, int retries, int life);
  
  public AsyncFuture<ITransport> sendAck( IModelObject envelope);
    
  public EventPipe getEventPipe();
  
  public Protocol getProtocol();
}
