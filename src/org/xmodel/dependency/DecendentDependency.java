/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * DecendentDependency.java
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

import org.xmodel.INode;

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
    INode target = (INode)targetObject;
    INode depend = (INode)dependObject;
    INode ancestor = depend.getParent();
    while( ancestor != null)
    {
      if ( ancestor.equals( target)) return true;
      ancestor = ancestor.getParent();
    }
    return false;
  }
}
