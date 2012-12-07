package org.xmodel.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;

/**
 * An ICompressor that compresses with a TabularCompressor and then post compresses using zip compression.
 */
public class ZipCompressor implements ICompressor
{
  public ZipCompressor( TabularCompressor compressor)
  {
    this.compressor = compressor;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#setFactory(org.xmodel.IModelObjectFactory)
   */
  @Override
  public void setFactory( IModelObjectFactory factory)
  {
    compressor.setFactory( factory);
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#setSerializer(org.xmodel.compress.ISerializer)
   */
  @Override
  public void setSerializer( ISerializer serializer)
  {
    compressor.setSerializer( serializer);
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#compress(org.xmodel.IModelObject, java.io.OutputStream)
   */
  @Override
  public void compress( IModelObject element, OutputStream stream) throws IOException
  {
    GZIPOutputStream gzip = new GZIPOutputStream( stream);
    try
    {
      compressor.compress( element, gzip);
    }
    finally
    {
      gzip.close();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#decompress(java.io.InputStream)
   */
  @Override
  public IModelObject decompress( InputStream stream) throws IOException
  {
    GZIPInputStream gzip = new GZIPInputStream( stream);
    try
    {
      return decompress( gzip);
    }
    finally
    {
      gzip.close();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#compress(org.xmodel.IModelObject)
   */
  @Override
  public List<byte[]> compress( IModelObject element) throws IOException
  {
    List<byte[]> buffers = compressor.compress( element);
    
    MultiByteArrayOutputStream stream = new MultiByteArrayOutputStream();
    
    GZIPOutputStream gzip = new GZIPOutputStream( stream);
    for( byte[] buffer: buffers) gzip.write( buffer);
    gzip.close();
    
    return stream.getBuffers();
  }

  private TabularCompressor compressor;
}
