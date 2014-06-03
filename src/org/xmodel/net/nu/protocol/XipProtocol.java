package org.xmodel.net.nu.protocol;

import java.io.ByteArrayOutputStream;

import org.xmodel.IModelObject;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.net.nu.IProtocol;

public class XipProtocol implements IProtocol
{

  @Override
  public byte[] encode( IModelObject message)
  {
    try
    {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      getThreadData().compress( message, stream);
      return stream.toByteArray();
    }
    catch( Exception e)
    {
    }
  }

  @Override
  public IModelObject decode( byte[] message, int offset, int length)
  {
    // TODO Auto-generated method stub
    return null;
  }

  private ICompressor getThreadData()
  {
    ICompressor compressor = threadData.get();
    if ( compressor == null)
    {
      compressor = new TabularCompressor();
      threadData.set( compressor);
    }
    return compressor;
  }
  

  private ThreadLocal<ICompressor> threadData;
}
