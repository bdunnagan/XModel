/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
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
    return object.cloneObject();
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObjectFactory#createExternalObject(org.xmodel.IModelObject, java.lang.String)
   */
  public IExternalReference createExternalObject( IModelObject parent, String type)
  {
    return new ExternalReference( type);
  }
}
