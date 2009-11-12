/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * CompoundDependency.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
