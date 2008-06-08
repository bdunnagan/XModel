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
 * An implementation of IChangeRecord for changing an attribute.
 */
public class ChangeAttributeRecord extends AbstractChangeRecord
{
  /**
   * Create an unbound change record for the specified identity path.  The
   * change record represents the setting of an attribute without a value.
   * @param path The identity path of the target object.
   * @param attrName The attribute which was set.
   */
  public ChangeAttributeRecord( IPath path, String attrName)
  {
    super( path);
    this.attrName = attrName;
  }
  
  /**
   * Create an unbound change record for the specified identity path.  The
   * change record represents the setting of an attribute with a specific value.
   * @param path The identity path of the target object.
   * @param attrName The attribute which was set.
   * @param attrValue The value of the attribute.
   */
  public ChangeAttributeRecord( IPath path, String attrName, Object attrValue)
  {
    super( path);
    this.attrName = attrName;
    this.attrValue = attrValue;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeRecord#getType()
   */
  public int getType()
  {
    return CHANGE_ATTRIBUTE;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeRecord#getAttributeName()
   */
  public String getAttributeName()
  {
    return attrName;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeRecord#getAttributeValue()
   */
  public Object getAttributeValue()
  {
    return attrValue;
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
    target.setAttribute( attrName, attrValue);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    String string = (attrName.length() > 0)? attrName: "text()";
    return "set: attribute: "+string+", value: "+attrValue+", path: "+path;
  }
  
  String attrName;
  Object attrValue;
}
