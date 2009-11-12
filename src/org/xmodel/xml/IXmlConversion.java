/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IXmlConversion.java
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
package org.xmodel.xml;

import java.util.List;

import org.jdom.Element;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;


/**
 * An interface for transforming a DOM model into an IModelObject model. The implementation 
 * has full control over the interpretation of the model and its transformation and is not
 * constrained to a one-to-one mapping.  Note that all conversions should treat the attribute
 * xm:name as the name of the object.
 * @deprecated
 */
@SuppressWarnings("unchecked")
public interface IXmlConversion
{
  /**
   * Specify the factory to use when converting from Elements to IModelObjects.
   * @param factory The factory for creating IModelObjects.
   */
  public void setFactory( IModelObjectFactory factory);
  
  /**
   * Transform a subset of an IModelObject model focused at the specified object
   * into one or more Element instances.  Additional objects which are consumed
   * during the transform can be added to the mutable consumed list.  These objects
   * will not be visited in subsequent calls to the IXmlConversion objects registered
   * with the IXmlConverter.  If there is no conversion for the given object then
   * null is returned.
   * @param object The focus of this transformation.
   * @param objects The list of objects consumed during this transform.
   * @return Returns one or more Node instances rooted on the returned object or null.
   */
  public Element transform( IModelObject object, List consumed);
  
  /**
   * Transform a subset of a DOM model focused at the specified Element into one or more
   * IModelObject instances.  Additional elements which are consumed during the transform 
   * can be added to the mutable consumed list.  These elements will not be visited in 
   * subsequent calls to the IXmlConversion objects registered with the IXmlConverter.
   * If there is no conversion for the given element then null is returned.
   * @param element The focus of this transformation.
   * @param elements The list of consumed elements.
   * @return Returns one or more IModelObject instances rooted on the returned object or null.
   */
  public IModelObject transform( Element element, List consumed);
}
