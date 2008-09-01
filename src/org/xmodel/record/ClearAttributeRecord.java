/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.record;

import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;

/**
 * An implementation of IChangeRecord for clearing an attribute.
 */
public class ClearAttributeRecord extends AbstractChangeRecord
{
  /**
   * Create an unbound change record for the specified identity path.  The
   * change record represents the clearing of an attribute.
   * @param path The identity path of the target object.
   * @param attrName The attribute which was set.
   */
  public ClearAttributeRecord( IPath path, String attrName)
  {
    super( path);
    this.attrName = attrName;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getType()
   */
  public int getType()
  {
    return CLEAR_ATTRIBUTE;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getAttributeName()
   */
  public String getAttributeName()
  {
    return attrName;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#applyChange(org.xmodel.IModelObject)
   */
  public void applyChange( IModelObject root)
  {
    if ( path == null) return;
    
    // create the subtree
    ModelAlgorithms.createPathSubtree( root, path, null, null);
    
    // apply change
    IModelObject target = path.queryFirst( root); 
    target.removeAttribute( attrName);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "clear: attribute: "+attrName+", path: "+path;
  }
  
  String attrName;
}
