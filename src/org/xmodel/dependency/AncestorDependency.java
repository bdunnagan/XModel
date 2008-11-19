/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.dependency;

import org.xmodel.IModelObject;

/**
 * An implementation of IDependency which evaluates true if the dependent object is
 * an ancestor of the target object.  When used with an IDependencySorter, objects
 * will be sorted ancestor first.
 */
public class AncestorDependency implements IDependency
{
  /**
   * Returns true if the dependent object is an ancestor of the target object.
   * @return Returns true if the dependent is an ancestor of the target.
   */
  public boolean evaluate( Object targetObject, Object dependObject)
  {
    IModelObject target = (IModelObject)targetObject;
    IModelObject depend = (IModelObject)dependObject;
    IModelObject ancestor = target.getParent();
    while( ancestor != null)
    {
      if ( ancestor.equals( depend)) return true;
      ancestor = ancestor.getParent();
    }
    return false;
  }
}
