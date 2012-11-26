/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IntersectChangeSet.java
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
 * A ChangeSet which produces records which, when applied, result in an intersection operation.
 * In the context of a diff operation, the intersection of two matching parent elements is defined
 * as the children in the left-hand tree for which there is a matching child in the right-hand tree.
 * Attribute updates are only performed if the attribute already exists in the left-hand element.
 */
public class IntersectChangeSet extends ChangeSet
{
  /* (non-Javadoc)
   * @see org.xmodel.ChangeSet#addChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  @Override
  public void addChild( INode object, INode child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.ChangeSet#addChild(org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  @Override
  public void addChild( INode object, INode child)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.ChangeSet#setAttribute(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
   */
  @Override
  public void setAttribute( INode object, String attrName, Object attrValue)
  {
    if ( object.getAttribute( attrName) != null)
      super.setAttribute( object, attrName, attrValue);
  }
}
