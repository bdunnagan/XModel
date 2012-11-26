/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * RemoveChildRecord.java
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

import org.xmodel.INode;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;

/**
 * An implementation of IChangeRecord for adding children.
 */
public class RemoveChildRecord extends AbstractChangeRecord
{
  /**
   * Create an unbound change record for the specified identity path. The change record represents
   * the removing of a child object.
   * @param path The identity path of the target object.
   * @param child The child to be removed.
   */
  public RemoveChildRecord( IPath path, INode child)
  {
    super( path);
    this.child = child;
    this.index = -1;
  }
  
  /**
   * Create an unbound change record for the specified identity path. The change record represents
   * the removing of a child object at the specified index.
   * @param path The identity path of the target object.
   * @param child The child to be removed.
   */
  public RemoveChildRecord( IPath path, int index)
  {
    super( path);
    this.child = null;
    this.index = index;
  }
  
  /**
   * Create an unbound change record for the specified identity path and relative child path.  The
   * child addressed by the relative child path will be removed from the parent addressed by the
   * identity path.
   * @param path The identity path of the parent.
   * @param childPath The relative child path.
   */
  public RemoveChildRecord( IPath path, IPath childPath)
  {
    super( path);
    this.childPath = childPath;
    this.child = null;
    this.index = -1;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getType()
   */
  public int getType()
  {
    return REMOVE_CHILD;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getChild()
   */
  public INode getChild()
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
  public void applyChange( INode root)
  {
    if ( path == null) return;
    
    // create the subtree
    ModelAlgorithms.createPathSubtree( root, path, null, null);
    
    // apply change
    INode target = path.queryFirst( root); 
    if ( index < 0)
    {
      if ( child == null)
      {
        INode child = childPath.queryFirst( target);
        if ( child != null) target.removeChild( child);
      }
      else
      {
        target.removeChild( child);
      }
    }
    else
    {
      target.removeChild( index);
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    if ( index < 0)
      return "remove: child: "+child+", path: "+path;
    else
      return "remove: child: "+child+", index: "+index+", path: "+path;
  }

  int index;
  INode child;
  IPath childPath;
}
