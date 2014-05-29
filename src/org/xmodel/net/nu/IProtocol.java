package org.xmodel.net.nu;

import org.xmodel.IModelObject;

public interface IProtocol
{
  public byte[] encode( IModelObject message);
  
  public IModelObject decode( byte[] message, int offset, int length);
}
