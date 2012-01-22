/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ModelObjectFactory.java
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
package org.xmodel;

import org.xml.sax.Attributes;
import org.xmodel.external.ExternalReference;
import org.xmodel.external.IExternalReference;


/**
 * An implementation of IModelObjectFactory which creates instances of ModelObject.
 */
public class ModelObjectFactory implements IModelObjectFactory
{
  /* (non-Javadoc)
   * @see org.xmodel.IModelObjectFactory#createObject(org.xmodel.IModelObject, 
   * java.lang.String)
   */
  public IModelObject createObject( IModelObject parent, String type)
  {
    return new ModelObject( type);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObjectFactory#createObject(org.xmodel.IModelObject, org.xml.sax.Attributes, java.lang.String)
   */
  public IModelObject createObject( IModelObject parent, Attributes attributes, String type)
  {
    return new ModelObject( type);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObjectFactory#createClone(org.xmodel.IModelObject)
   */
  public IModelObject createClone( IModelObject object)
  {
    IModelObject clone = new ModelObject( object.getType());
    ModelAlgorithms.copyAttributes( object, clone);
    return clone;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObjectFactory#createExternalObject(org.xmodel.IModelObject, java.lang.String)
   */
  public IExternalReference createExternalObject( IModelObject parent, String type)
  {
    return new ExternalReference( type);
  }
}
