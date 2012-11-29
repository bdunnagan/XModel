package org.xmodel.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.log.SLog;

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
  public ChannelBuffer compress( IModelObject element) throws IOException
  {
    ChannelBuffer buffer = compressor.compress( element);
    
    ChannelBuffer output = ChannelBuffers.dynamicBuffer( (int)(buffer.readableBytes() * 0.15));
    GZIPOutputStream gzip = new GZIPOutputStream( new ChannelBufferOutputStream( output));
    buffer.readBytes( gzip, buffer.readableBytes());
    gzip.close();
    
    return output;
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#decompress(org.jboss.netty.buffer.ChannelBuffer)
   */
  @Override
  public IModelObject decompress( ChannelBuffer input) throws IOException
  {
    GZIPInputStream gzip = new GZIPInputStream( new ChannelBufferInputStream( input));
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer( 1024);
    try
    {
      while ( buffer.writeBytes( gzip, 1024) == 1024);
    }
    catch( Exception e)
    {
      SLog.warn( this, e);
    }
    
    return compressor.decompress( buffer);
  }

  private TabularCompressor compressor;
}
