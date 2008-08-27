/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import org.xml.sax.Attributes;

import dunnagan.bob.xmodel.external.ExternalElement;
import dunnagan.bob.xmodel.external.IExternalReference;

/**
 * An implementation of IModelObjectFactory which creates instances of ModelObject.
 */
public class ElementFactory implements IModelObjectFactory
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObjectFactory#createObject(dunnagan.bob.xmodel.IModelObject, 
   * java.lang.String)
   */
  public IModelObject createObject( IModelObject parent, String type)
  {
    return new Element( type);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObjectFactory#createObject(dunnagan.bob.xmodel.IModelObject, org.xml.sax.Attributes, java.lang.String)
   */
  public IModelObject createObject( IModelObject parent, Attributes attributes, String type)
  {
    return new Element( type);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObjectFactory#createClone(dunnagan.bob.xmodel.IModelObject)
   */
  public IModelObject createClone( IModelObject object)
  {
    Element clone = new Element( object.getType());
    ModelAlgorithms.copyAttributes( object, clone);
    return clone;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObjectFactory#createExternalObject(dunnagan.bob.xmodel.IModelObject, java.lang.String)
   */
  public IExternalReference createExternalObject( IModelObject parent, String type)
  {
    return new ExternalElement( type);
  }
}
