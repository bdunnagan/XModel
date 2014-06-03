package org.xmodel.net.nu;

import java.io.IOException;

import org.xmodel.IModelObject;

public interface IProtocol
{
  public byte[] encode( IModelObject message) throws IOException;
  
  public IModelObject decode( byte[] message, int offset, int length) throws IOException;
}
