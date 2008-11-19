/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel;

import org.xml.sax.Attributes;
import org.xmodel.external.IExternalReference;


/**
 * An interface for creating IModelObject(s).
 */
public interface IModelObjectFactory
{
  /**
   * Create a new object of the specified type. Note that it should not be the responsibility of the
   * factory to add the new object to the specified parent. The parent is provided so that the
   * factory can decide which object to create based on the full path.
   * @param parent The parent to-be of the new object or null.
   * @param type The type of the object.
   * @return Returns a new object of the specified type.
   */
  public IModelObject createObject( IModelObject parent, String type);

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
  public IModelObject createObject( IModelObject parent, Attributes attributes, String type);
  
  /**
   * Create a shallow clone of the specified object.
   * @param object The object to be cloned.
   * @return Returns a shallow clone of the specified objects.
   */
  public IModelObject createClone( IModelObject object);

  /**
   * Create a new IExternalReference of the specified type. Note that it should not be the
   * responsibility of the factory to add the new object to the specified parent. The parent is
   * provided so that the factory can decide which object to create based on the full path.
   * @param parent The parent to-be of the new object or null.
   * @param type The type of the object.
   * @return Returns a new object of the specified type.
   */
  public IExternalReference createExternalObject( IModelObject parent, String type);
}
