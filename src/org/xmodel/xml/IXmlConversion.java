/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
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
