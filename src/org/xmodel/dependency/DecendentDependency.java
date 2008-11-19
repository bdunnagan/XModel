/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.dependency;

import org.xmodel.IModelObject;

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
