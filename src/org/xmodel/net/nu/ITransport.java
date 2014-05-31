package org.xmodel.net.nu;

import java.io.IOException;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;

/**
 * (thread-safe)
 */
public interface ITransport
{
  public AsyncFuture<ITransport> connect( int timeout) throws IOException;
  
  public AsyncFuture<ITransport> disconnect() throws IOException;

  public AsyncFuture<ITransport> send( IModelObject message, int timeout) throws IOException;
  
  public IContextFactory getContexts();

  public void addListener( IReceiveListener listener);
  
  public void removeListener( IReceiveListener listener);
  
  public void addListener( IConnectListener listener);
  
  public void removeListener( IConnectListener listener);
  
  public void addListener( IDisconnectListener listener);
  
  public void removeListener( IDisconnectListener listener);
}
