package org.xmodel.compress;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.xmodel.external.AbstractCachingPolicy;
import org.xmodel.external.CachingException;
import org.xmodel.external.IExternalReference;
import org.xmodel.storage.ByteArrayStorageClass;

public class ByteArrayCachingPolicy extends AbstractCachingPolicy
{
  public ByteArrayCachingPolicy( TabularCompressor compressor)
  {
    this.compressor = compressor;
    setStaticAttributes( new String[] { "*"});
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#sync(org.xmodel.external.IExternalReference)
   */
  @Override
  public void sync( IExternalReference reference) throws CachingException
  {
    ByteArrayStorageClass storageClass = (ByteArrayStorageClass)reference.getStorageClass();
    byte[] bytes = storageClass.getBytes();
    int offset = storageClass.getByteOffset();

    try
    {
      DataInputStream bytesIn = new DataInputStream( new ByteArrayInputStream( bytes, offset, bytes.length - offset));
      compressor.readChildren( bytesIn, reference);
    }
    catch( IOException e)
    {
      throw new CachingException( String.format( "Unable to sync reference, %s", reference.getType()), e);
    }
  }
  
  private TabularCompressor compressor;
}
