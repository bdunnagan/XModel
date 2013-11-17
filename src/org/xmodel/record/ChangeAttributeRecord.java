/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ChangeAttributeRecord.java
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
   * @see org.xmodel.IChangeRecord#getType()
   */
  public int getType()
  {
    return CHANGE_ATTRIBUTE;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getAttributeName()
   */
  public String getAttributeName()
  {
    return attrName;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getAttributeValue()
   */
  public Object getAttributeValue()
  {
    return attrValue;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#applyChange(org.xmodel.IModelObject)
   */
  public void applyChange( IModelObject root)
  {
    if ( path == null) return;
    
    // create the subtree
    ModelAlgorithms.createPathSubtree( root, path, null, null, null);

    // TODO: combine with subtree creation above?
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
