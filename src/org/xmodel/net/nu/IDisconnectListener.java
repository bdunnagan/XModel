package org.xmodel.net.nu;

import org.xmodel.xpath.expression.IContext;

public interface IDisconnectListener
{
  public void onDisconnect( ITransport transport, IContext context) throws Exception;
}
