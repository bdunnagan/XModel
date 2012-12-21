/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ExternalReference.java
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

import org.xmodel.ModelObject;

/**
 * An implementation of IExternalReference which extends ModelObject to provide listener semantics.
 * @deprecated Use ModelObject instead.
 */
public class ExternalReference extends ModelObject implements IExternalReference
{  
  /**
   * Create the ExternalReference with the specified type which is not dirty.
   * @param type The type of this reference.
   */
  public ExternalReference( String type)
  {
    super( type);
  }
}
