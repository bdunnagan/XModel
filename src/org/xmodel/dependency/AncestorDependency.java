/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AncestorDependency.java
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
