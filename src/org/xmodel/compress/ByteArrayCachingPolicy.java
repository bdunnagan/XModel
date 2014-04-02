package org.xmodel.compress;

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
    System.out.println( "Sync: "+reference.getType());
    
    try
    {
      // TODO: discard storage class and extra data
      ByteArrayStorageClass storageClass = (ByteArrayStorageClass)reference.getStorageClass();
      CaptureInputStream in = new CaptureInputStream( storageClass.getStream());
      compressor.readChildren( in, reference);
    }
    catch( IOException e)
    {
      throw new CachingException( String.format( "Unable to sync reference, %s", reference.getType()), e);
    }
  }
  
  protected TabularCompressor compressor;
}
