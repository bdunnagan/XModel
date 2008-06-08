/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import dunnagan.bob.xmodel.IModelObject;

/**
 * Abstract base implementation with get/set for compression level.
 */
public abstract class AbstractCompressor implements ICompressor
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.compress.ICompressor#compress(dunnagan.bob.xmodel.IModelObject)
   */
  public byte[] compress( IModelObject element) throws CompressorException
  {
    if ( stream == null) stream = new ByteArrayOutputStream();
    compress( element, stream);
    byte[] bytes = stream.toByteArray();
    stream.reset();
    return bytes;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.compress.ICompressor#decompress(byte[], int)
   */
  public IModelObject decompress( byte[] bytes, int offset) throws CompressorException
  {
    ByteArrayInputStream stream = new ByteArrayInputStream( bytes, offset, bytes.length - offset);
    return decompress( stream);
  }
  
  private ByteArrayOutputStream stream;
}
