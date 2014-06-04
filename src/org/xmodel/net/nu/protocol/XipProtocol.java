package org.xmodel.net.nu.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.xmodel.IModelObject;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.net.nu.IProtocol;

public class XipProtocol implements IProtocol
{
  public XipProtocol()
  {
    this( new TabularCompressor());
  }
  
  public XipProtocol( ICompressor compressor)
  {
    this.compressor = compressor;
  }
  
  @Override
  public byte[] encode( IModelObject message) throws IOException
  {
    // TODO: framing
    throw new UnsupportedOperationException();
    
//    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//    compressor.compress( message, stream);
//    return stream.toByteArray();
  }

  @Override
  public IModelObject decode( byte[] message, int offset, int length) throws IOException
  {
    return compressor.decompress( new ByteArrayInputStream( message, offset, length));
  }

  private ICompressor compressor;
}
