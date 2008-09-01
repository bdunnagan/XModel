/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
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
