/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.dependency;

/**
 * An interface which defines a dependency between objects.
 */
public interface IDependency
{
  /**
   * Returns true if the dependent argument is subordinate to the target argument.
   * @param target The target object.
   * @param dependent The (possibly) dependent object.
   * @return Returns true if the dependent argument is subordinate to the target argument.
   */
  public boolean evaluate( Object target, Object dependent);
}
