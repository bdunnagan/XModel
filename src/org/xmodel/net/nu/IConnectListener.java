package org.xmodel.net.nu;

import org.xmodel.xpath.expression.IContext;

public interface IConnectListener
{
  public void onConnect( ITransport transport, IContext context) throws Exception;
}
