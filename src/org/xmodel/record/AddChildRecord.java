/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AddChildRecord.java
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
package org.xmodel.record;

import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;

/**
 * An implementation of IChangeRecord for adding children. If the path to the child does not exist
 * in the target object when the record is applied, then the path will be created.
 */
public class AddChildRecord extends AbstractChangeRecord
{
  /**
   * Create an unbound change record for the specified identity path. The change record represents
   * the adding of a child object.
   * @param path The identity path of the target object.
   * @param child The child to be removed.
   */
  public AddChildRecord( IPath path, IModelObject child)
  {
    super( path);
    this.child = child;
    index = -1;
  }
  
  /**
   * Create an unbound change record for the specified identity path. The change record represents
   * the adding of a child object at the specified index.
   * @param path The identity path of the target object.
   * @param child The child to be removed.
   * @param index The index where the child will be added.
   */
  public AddChildRecord( IPath path, IModelObject child, int index)
  {
    super( path);
    this.child = child;
    this.index = index;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getType()
   */
  public int getType()
  {
    return ADD_CHILD;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getChild()
   */
  public IModelObject getChild()
  {
    return super.getChild();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.transaction.AbstractChangeRecord#getIndex()
   */
  public int getIndex()
  {
    return index;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#applyChange(org.xmodel.IModelObject)
   */
  public void applyChange( IModelObject root)
  {
    if ( path == null) return;

    // create the subtree
    ModelAlgorithms.createPathSubtree( root, path, null, null, null);

    // apply change
    IModelObject target = path.queryFirst( root);
    if ( index < 0) target.addChild( child); else target.addChild( child, index);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "add child: "+child+", index: "+index+", path: "+path;
  }
  
  IModelObject child;
  int index;
}
