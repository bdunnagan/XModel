package org.xmodel.net.nu.tcp;

import java.io.IOException;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.xpath.expression.IContext;

public interface ITcpServerEventHandler
{
  public void notifyConnect( ITransport transport, IContext transportContext) throws IOException;

  public void notifyDisconnect( ITransport transport, IContext transportContext) throws IOException;

  public void notifyReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject requestMessage);

  public void notifyError( ITransport transport, IContext context, Error error, IModelObject request);

  public void notifyException( ITransport transport, IOException e);
}
