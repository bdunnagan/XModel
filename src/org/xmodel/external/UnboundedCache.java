/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * UnboundedCache.java
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
package org.xmodel.external;

import org.xmodel.IModelObject;

/**
 * An implementation of ICache which is unbounded (never clears its references).
 */
public class UnboundedCache implements ICache
{
  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#configure(org.xmodel.IModelObject)
   */
  public void configure( IModelObject annotation)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#add(org.xmodel.external.IExternalReference)
   */
  public void add( IExternalReference reference)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#remove(org.xmodel.external.IExternalReference)
   */
  public void remove( IExternalReference reference)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#touch(org.xmodel.external.IExternalReference)
   */
  public void touch( IExternalReference reference)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#size()
   */
  public int size()
  {
    return -1;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#capacity()
   */
  public int capacity()
  {
    return -1;
  }
}
