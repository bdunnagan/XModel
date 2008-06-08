/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.dependency;


/**
 * An implementation of IDependency which defines dependency relationships between classes.
 */
@SuppressWarnings("unchecked")
public class ClassDependency implements IDependency
{
  public ClassDependency( Class first, Class second)
  {
    this.first = first;
    this.second = second;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IDependency#evaluate(java.lang.Object, java.lang.Object)
   */
  public boolean evaluate( Object target, Object dependent)
  {
    return target.getClass().equals( second) && dependent.getClass().equals( first);
  }
  
  Class first;
  Class second;
}
