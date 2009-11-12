/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AbstractCompressor.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;


/**
 * Abstract base implementation with get/set for compression level.
 */
public abstract class AbstractCompressor implements ICompressor
{
  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#setFactory(org.xmodel.IModelObjectFactory)
   */
  public void setFactory( IModelObjectFactory factory)
  {
    this.factory = factory;
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#compress(org.xmodel.IModelObject)
   */
  public byte[] compress( IModelObject element) throws CompressorException
  {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    compress( element, stream);
    byte[] bytes = stream.toByteArray();
    return bytes;
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#decompress(byte[], int)
   */
  public IModelObject decompress( byte[] bytes, int offset) throws CompressorException
  {
    ByteArrayInputStream stream = new ByteArrayInputStream( bytes, offset, bytes.length - offset);
    IModelObject result = decompress( stream);
    return result;
  }
  
  protected IModelObjectFactory factory;
}
