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

import org.xmodel.IModelObjectFactory;

/**
 * Abstract base implementation with get/set for compression level.
 */
public abstract class AbstractCompressor implements ICompressor
{
  protected AbstractCompressor()
  {
    serializer = new DefaultSerializer();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#setFactory(org.xmodel.IModelObjectFactory)
   */
  public void setFactory( IModelObjectFactory factory)
  {
    this.factory = factory;
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#setSerializer(org.xmodel.compress.ISerializer)
   */
  @Override
  public void setSerializer( ISerializer serializer)
  {
    this.serializer = serializer;
  }
  
  protected IModelObjectFactory factory;
  protected ISerializer serializer;
}
