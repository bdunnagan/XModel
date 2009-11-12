/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ClearAttributeBoundRecord.java
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

import org.xmodel.IBoundChangeRecord;
import org.xmodel.IChangeRecord;
import org.xmodel.IModelObject;

/**
 * An implementation of IBoundChangeRecord for clearing attributes.
 */
public class ClearAttributeBoundRecord extends AbstractBoundRecord
{
  /**
   * @param object
   * @param attrName
   */
  public ClearAttributeBoundRecord( IModelObject object, String attrName)
  {
    super( object);
    this.attrName = attrName;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUndoRecord()
   */
  public IBoundChangeRecord createUndoRecord()
  {
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null)
      return new ChangeAttributeBoundRecord( object, attrName);
    else
      return new ChangeAttributeBoundRecord( object, attrName, attrValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUnboundRecord()
   */
  public IChangeRecord createUnboundRecord()
  {
    return new ClearAttributeRecord( getPath(), attrName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUnboundRecord(org.xmodel.IModelObject)
   */
  public IChangeRecord createUnboundRecord( IModelObject relative)
  {
    return new ClearAttributeRecord( getRelativePath( relative), attrName);
  }

  public int getType()
  {
    return CLEAR_ATTRIBUTE;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.record.AbstractChangeRecord#getAttributeName()
   */
  @Override
  public String getAttributeName()
  {
    return attrName;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#execute()
   */
  public void applyChange()
  {
    //TODO: change removeAttribute to clearAttribute on IModelObject
    getBoundObject().removeAttribute( attrName);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    IModelObject object = getBoundObject();
    return "clear: attribute: "+attrName+", object: "+object;
  }
  
  String attrName;
}
