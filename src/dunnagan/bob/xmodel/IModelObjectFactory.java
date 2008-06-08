/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import org.xml.sax.Attributes;

/**
 * An interface for creating IModelObject(s).
 */
public interface IModelObjectFactory
{
  /**
   * Create a new object of the specified type. Note that it should not be the responsibility of the
   * factory to add the new object to the specified parent. The parent is provided so that the
   * factory can decide which object to create based on the full path.
   * @parma parent The parent to-be of the new object or null.
   * @param type The type of the object.
   * @return Returns a new object of the specified type.
   */
  public IModelObject createObject( IModelObject parent, String type);

  /**
   * Create a new object of the specified type. Note that it should not be the responsibility of the
   * factory to add the new object to the specified parent. The parent is provided so that the
   * factory can decide which object to create based on the full path. The same is true of the
   * attributes. The attributes are provided so that namespaces can be determined.
   * @parma parent The parent to-be of the new object or null.
   * @param attributes The attributes that will be added to the object.
   * @param type The type of the object.
   * @return Returns a new object of the specified type.
   */
  public IModelObject createObject( IModelObject parent, Attributes attributes, String type);
  
  /**
   * Creat a shallow clone of the specified object (only copy attributes).
   * @param object The object to be cloned.
   * @return Returns a shallow clone of the specified objects.
   */
  public IModelObject createClone( IModelObject object);
}
