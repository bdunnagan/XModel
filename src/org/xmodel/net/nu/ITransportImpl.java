package org.xmodel.net.nu;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.xpath.expression.IContext;

public interface ITransportImpl extends ITransport
{
  public AsyncFuture<ITransport> sendImpl( IModelObject message, IModelObject request);
  
  public IContext getTransportContext();
}
