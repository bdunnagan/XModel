package org.xmodel.net.nu;

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;

public interface ITimeoutListener
{
  public void onTimeout( ITransport transport, IModelObject message, IContext context) throws Exception;
}
