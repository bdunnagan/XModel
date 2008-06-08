/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import org.xml.sax.Attributes;

/**
 * An implementation of IModelObjectFactory which creates instances of ModelObject.
 */
public class ModelObjectFactory implements IModelObjectFactory
{
  /**
   * Create a factory for creating objects in the default model.
   */
  public ModelObjectFactory()
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObjectFactory#createObject(dunnagan.bob.xmodel.IModelObject, 
   * java.lang.String)
   */
  public IModelObject createObject( IModelObject parent, String type)
  {
    return new ModelObject( type);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObjectFactory#createObject(dunnagan.bob.xmodel.IModelObject, org.xml.sax.Attributes, java.lang.String)
   */
  public IModelObject createObject( IModelObject parent, Attributes attributes, String type)
  {
    return new ModelObject( type);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObjectFactory#createClone(dunnagan.bob.xmodel.IModelObject)
   */
  public IModelObject createClone( IModelObject object)
  {
    return object.cloneObject();
  }
}
