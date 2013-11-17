/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ClearAttributeRecord.java
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
    ModelAlgorithms.createPathSubtree( root, path, null, null, null);
    
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
