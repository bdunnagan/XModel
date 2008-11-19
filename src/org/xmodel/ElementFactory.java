/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel;

import org.xml.sax.Attributes;
import org.xmodel.external.ExternalElement;
import org.xmodel.external.IExternalReference;


/**
 * An implementation of IModelObjectFactory which creates instances of ModelObject.
 */
public class ElementFactory implements IModelObjectFactory
{
  /* (non-Javadoc)
   * @see org.xmodel.IModelObjectFactory#createObject(org.xmodel.IModelObject, 
   * java.lang.String)
   */
  public IModelObject createObject( IModelObject parent, String type)
  {
    return new Element( type);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObjectFactory#createObject(org.xmodel.IModelObject, org.xml.sax.Attributes, java.lang.String)
   */
  public IModelObject createObject( IModelObject parent, Attributes attributes, String type)
  {
    return new Element( type);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObjectFactory#createClone(org.xmodel.IModelObject)
   */
  public IModelObject createClone( IModelObject object)
  {
    Element clone = new Element( object.getType());
    ModelAlgorithms.copyAttributes( object, clone);
    return clone;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelObjectFactory#createExternalObject(org.xmodel.IModelObject, java.lang.String)
   */
  public IExternalReference createExternalObject( IModelObject parent, String type)
  {
    return new ExternalElement( type);
  }
}
