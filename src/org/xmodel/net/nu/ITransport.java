package org.xmodel.net.nu;

import java.io.IOException;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;

public interface ITransport
{
  public AsyncFuture<ITransport> connect( int timeout) throws IOException;
  
  public AsyncFuture<ITransport> disconnect() throws IOException;

  public AsyncFuture<ITransport> send( IModelObject message, int timeout) throws IOException;
  
  public IContextFactory getContexts();

  public void addListener( IReceiveListener listener);
  
  public void removeListener( IReceiveListener listener);
  
  public void addConnectListener( IConnectListener listener);
  
  public void removeConnectListener( IConnectListener listener);
  
  public void addDisconnectListener( IDisconnectListener listener);
  
  public void removeDisconnectListener( IDisconnectListener listener);
}
