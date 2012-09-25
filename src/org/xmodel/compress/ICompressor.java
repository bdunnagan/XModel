/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ICompressor.java
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

import java.io.IOException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;

/**
 * An interface for algorithms which compress an IModelObject tree into a byte sequence.
 */
public interface ICompressor
{
  /**
   * Set the factory used to create decompressed objects.
   * @param factory The factory.
   */
  public void setFactory( IModelObjectFactory factory);

  /**
   * Set the instance of ISerializer for serializing attribute values.
   * @param serializer The serializer.
   */
  public void setSerializer( ISerializer serializer);

  /**
   * Compress the specified element into the specified output buffer.
   * @param element The element.
   * @return Returns the channel buffer containing the compressed data.
   */
  public ChannelBuffer compress( IModelObject element) throws IOException;
  
  /**
   * Decompress an element from the specified input buffer.
   * @param input The input buffer.
   * @return Returns the decompressed element.
   */
  public IModelObject decompress( ChannelBuffer input) throws IOException;
}
