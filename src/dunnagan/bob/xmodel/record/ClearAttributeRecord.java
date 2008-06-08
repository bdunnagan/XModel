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
   * @see dunnagan.bob.xmodel.IChangeRecord#getType()
   */
  public int getType()
  {
    return CLEAR_ATTRIBUTE;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeRecord#getAttributeName()
   */
  public String getAttributeName()
  {
    return attrName;
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
