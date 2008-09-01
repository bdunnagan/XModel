/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.dependency;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of IDependency which evaluates true if ALL of the dependencies
 * associated with the CompoundDependency object evaluate true.  By default, the 
 * DependencySorter evaluates rules using a logical OR operation.  Using this class,
 * IDependency rules can be evaluated using a logical AND operation.
 */
public class CompoundDependency implements IDependency
{
  public CompoundDependency()
  {
    rules = new ArrayList<IDependency>();
  }
  
  /**
   * Add an IDependency rule to this CompoundDependency object.
   * @param rule The rule to be added.
   */
  public void add( IDependency rule)
  {
    rules.add( rule);
  }

  /**
   * Remove an IDependency rule from this CompoundDependency object.
   * @param rule The rule to be removed.
   */
  public void remove( IDependency rule)
  {
    rules.remove( rule);
  }
  
  /**
   * Returns true if all of this object's IDependency rules evaluate true.
   * @return Returns true if all of this object's IDependency rules evaluate true.
   */
  public boolean evaluate( Object target, Object dependent)
  {
    for ( int i=0; i<rules.size(); i++)
    {
      IDependency rule = (IDependency)rules.get( i);
      if ( !rule.evaluate( target, dependent)) return false;
    }
    return true;
  }
  
  List<IDependency> rules;
}
