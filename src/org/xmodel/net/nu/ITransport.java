package org.xmodel.net.nu;

import java.io.IOException;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.xpath.expression.IContext;

/**
 * (thread-safe)
 */
public interface ITransport
{
  public enum Error { timeout, connectRefused, connectError, encodeFailed};
  
  public AsyncFuture<ITransport> connect( int timeout) throws IOException;
  
  public AsyncFuture<ITransport> disconnect() throws IOException;

  public AsyncFuture<ITransport> send( IModelObject message);
  
  public AsyncFuture<ITransport> request( IModelObject message, IContext messageContext, int timeout);
  
  public AsyncFuture<ITransport> respond( IModelObject message, IModelObject request);
  
  public void addListener( IConnectListener listener);
  
  public void removeListener( IConnectListener listener);
  
  public void addListener( IDisconnectListener listener);
  
  public void removeListener( IDisconnectListener listener);
  
  public void addListener( IReceiveListener listener);
  
  public void removeListener( IReceiveListener listener);
  
  public void addListener( IErrorListener listener);
  
  public void removeListener( IErrorListener listener);
}
