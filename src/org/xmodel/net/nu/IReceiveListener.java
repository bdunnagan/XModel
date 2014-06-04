package org.xmodel.net.nu;

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;

public interface IReceiveListener
{
  public void onReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject request) throws Exception;
}
