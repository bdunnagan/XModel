/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AnyCheck.java
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
package org.xmodel.xsd.check;

import org.xmodel.IModelObject;

public class AnyCheck extends ConstraintCheck
{
  public AnyCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ConstraintCheck#validateOnce(org.xmodel.IModelObject, int, int)
   */
  @Override
  public boolean validateOnce( IModelObject documentLocus, int start, int end)
  {
    // TODO: need to handle different types of any constraints
    count = end - start; 
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ConstraintCheck#anyCount()
   */
  @Override
  public int anyCount()
  {
    return count;
  }
  
  private int count;
}
