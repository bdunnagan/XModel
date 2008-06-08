/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.record;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IPath;
import dunnagan.bob.xmodel.ModelAlgorithms;

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
  public RemoveChildRecord( IPath path, IModelObject child)
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
   * @see dunnagan.bob.xmodel.IChangeRecord#getType()
   */
  public int getType()
  {
    return REMOVE_CHILD;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeRecord#getChild()
   */
  public IModelObject getChild()
  {
    return super.getChild();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.transaction.AbstractChangeRecord#getIndex()
   */
  public int getIndex()
  {
    return index;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeRecord#applyChange(dunnagan.bob.xmodel.IModelObject)
   */
  public void applyChange( IModelObject root)
  {
    if ( path == null) return;
    
    // create the subtree
    ModelAlgorithms.createPathSubtree( root, path, null, null);
    
    // apply change
    IModelObject target = path.queryFirst( root); 
    if ( index < 0)
    {
      if ( child == null)
      {
        IModelObject child = childPath.queryFirst( target);
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
  IModelObject child;
  IPath childPath;
}
