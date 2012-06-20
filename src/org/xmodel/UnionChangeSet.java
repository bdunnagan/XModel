/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * UnionChangeSet.java
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
package org.xmodel;

/**
 * A ChangeSet which produces records which, when applied, result in a union operation.
 */
public class UnionChangeSet extends ChangeSet
{
  /* (non-Javadoc)
   * @see org.xmodel.ChangeSet#removeAttribute(org.xmodel.IModelObject, java.lang.String)
   */
  @Override
  public void removeAttribute( IModelObject object, String attrName)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.ChangeSet#removeChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  @Override
  public void removeChild( IModelObject object, IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.ChangeSet#removeChild(org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  @Override
  public void removeChild( IModelObject object, IModelObject child)
  {
  }
}
