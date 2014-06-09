package org.xmodel.net.nu;

import java.util.concurrent.ScheduledFuture;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

/**
 * (thread-safe)
 */
public interface ITransport
{
  public enum Error { timeout, connectRefused, connectError, encodeFailed, channelClosed, messageExpired};

  public Protocol getProtocol();
  
  public AsyncFuture<ITransport> connect( int timeout);
  
  public AsyncFuture<ITransport> disconnect();

  public AsyncFuture<ITransport> request( IModelObject message, IContext messageContext, int timeout);
  
  public AsyncFuture<ITransport> ack( IModelObject request);
  
  public AsyncFuture<ITransport> respond( IModelObject message, IModelObject request);
  
  public ScheduledFuture<?> schedule( Runnable runnable, int delay);
  
  public void notifyError( IContext context, ITransport.Error error, IModelObject request);
  
  public void addListener( IConnectListener listener);
  
  public void removeListener( IConnectListener listener);
  
  public void addListener( IDisconnectListener listener);
  
  public void removeListener( IDisconnectListener listener);
  
  public void addListener( IReceiveListener listener);
  
  public void removeListener( IReceiveListener listener);
  
  public void addListener( IErrorListener listener);
  
  public void removeListener( IErrorListener listener);
}
