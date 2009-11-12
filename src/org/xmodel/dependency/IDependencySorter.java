/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IDependencySorter.java
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

import java.util.Collection;
import java.util.List;

/**
 * An interface which provides an algorithm for sorting domain objects according
 * to a collection of dependency rules defined by IDependency objects.  Foreach
 * dependency, the sort algorithm guarantees that the dependent object will appear
 * before the target object in the result list.
 */
public interface IDependencySorter
{
  /**
   * Add a dependency rule to the collection of rules used to order domain objects.
   * @param rule The dependency rule.
   */
  public void add( IDependency rule);
  
  /**
   * Remove a dependency rule from the collection of rules used to order domain objects.
   * @param rule The dependency rule.
   */
  public void remove( IDependency rule);
  
  /**
   * Returns the number of IDependency rules.
   * @return Returns the number of IDependency rules.
   */
  public int count();

  /**
   * Sort a collection of objects according to the dependency rules which were
   * added to this object.  For the purposes of this algorithm, a dependency exists
   * if ANY rule establishes a dependency between a particular target object and
   * dependent object.  In other words, the rules are applied using a logical OR.
   * @param objects A list of objects which will be sorted.
   */
  public List<Object> sort( Collection<Object> objects);
}
