package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.xmodel.IModelObject;

public interface IWireProtocol
{
  public byte[] encode( IModelObject message) throws IOException;
  
  public IModelObject decode( byte[] message, int offset, int length) throws IOException;
  
  public IModelObject decode( ByteBuffer buffer) throws IOException;
}
