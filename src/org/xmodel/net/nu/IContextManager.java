package org.xmodel.net.nu;

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;

public interface IContextManager
{
  public IContext getTransportContext();
  
  public IContext beginMessageContext( IModelObject message);
  
  public IContext endMessageContext( IModelObject messsage);
  
  public IContext getMessageContext( IModelObject message);
}
