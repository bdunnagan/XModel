/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IModelObjectFactory.java
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
import org.xmodel.external.IExternalReference;


/**
 * An interface for creating IModelObject(s).
 */
public interface INodeFactory
{
  /**
   * Create a new object of the specified type. Note that it should not be the responsibility of the
   * factory to add the new object to the specified parent. The parent is provided so that the
   * factory can decide which object to create based on the full path.
   * @param parent The parent to-be of the new object or null.
   * @param type The type of the object.
   * @return Returns a new object of the specified type.
   */
  public INode createObject( INode parent, String type);

  /**
   * Create a new object of the specified type. Note that it should not be the responsibility of the
   * factory to add the new object to the specified parent. The parent is provided so that the
   * factory can decide which object to create based on the full path. The same is true of the
   * attributes. The attributes are provided so that namespaces can be determined.
   * @param parent The parent to-be of the new object or null.
   * @param attributes The attributes that will be added to the object.
   * @param type The type of the object.
   * @return Returns a new object of the specified type.
   */
  public INode createObject( INode parent, Attributes attributes, String type);
  
  /**
   * Create a shallow clone of the specified object.
   * @param object The object to be cloned.
   * @return Returns a shallow clone of the specified objects.
   */
  public INode createClone( INode object);

  /**
   * Create a new IExternalReference of the specified type. Note that it should not be the
   * responsibility of the factory to add the new object to the specified parent. The parent is
   * provided so that the factory can decide which object to create based on the full path.
   * @param parent The parent to-be of the new object or null.
   * @param type The type of the object.
   * @return Returns a new object of the specified type.
   */
  public IExternalReference createExternalObject( INode parent, String type);
}
