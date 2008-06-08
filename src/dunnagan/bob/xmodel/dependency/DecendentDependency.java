/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.dependency;

import dunnagan.bob.xmodel.IModelObject;

/**
 * An implementation of IDependency which evaluates true if the dependent object is
 * a decendent of the target object.  When used with an IDependencySorter, objects
 * will be sorted decendent first.
 */
public class DecendentDependency implements IDependency
{
  /**
   * Returns true if the dependent object is a decendent of the target object.
   * @return Returns true if the dependent is a decendent of the target.
   */
  public boolean evaluate( Object targetObject, Object dependObject)
  {
    IModelObject target = (IModelObject)targetObject;
    IModelObject depend = (IModelObject)dependObject;
    IModelObject ancestor = depend.getParent();
    while( ancestor != null)
    {
      if ( ancestor.equals( target)) return true;
      ancestor = ancestor.getParent();
    }
    return false;
  }
}
