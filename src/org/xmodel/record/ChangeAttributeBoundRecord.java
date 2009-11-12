/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ChangeAttributeBoundRecord.java
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
 * An implementation of IBoundChangeRecord for changing attributes.
 */
public class ChangeAttributeBoundRecord extends AbstractBoundRecord
{
  /**
   * @param object
   * @param attrName
   */
  public ChangeAttributeBoundRecord( IModelObject object, String attrName)
  {
    super( object);
    this.attrName = attrName;
  }
  
  /**
   * @param object
   * @param attrName
   * @param attrValue
   */
  public ChangeAttributeBoundRecord( IModelObject object, String attrName, Object attrValue)
  {
    super( object);
    this.attrName = attrName;
    this.attrValue = attrValue;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUndoRecord()
   */
  public IBoundChangeRecord createUndoRecord()
  {
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null)
      return new ClearAttributeBoundRecord( object, attrName);
    else
      return new ChangeAttributeBoundRecord( object, attrName, attrValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUnboundRecord()
   */
  public IChangeRecord createUnboundRecord()
  {
    if ( attrValue == null)
      return new ChangeAttributeRecord( getPath(), attrName);
    else
      return new ChangeAttributeRecord( getPath(), attrName, attrValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUnboundRecord(org.xmodel.IModelObject)
   */
  public IChangeRecord createUnboundRecord( IModelObject relative)
  {
    if ( attrValue == null)
      return new ChangeAttributeRecord( getRelativePath( relative), attrName);
    else
      return new ChangeAttributeRecord( getRelativePath( relative), attrName, attrValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getType()
   */
  public int getType()
  {
    return CHANGE_ATTRIBUTE;
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
   * @see org.xmodel.record.AbstractChangeRecord#getAttributeValue()
   */
  @Override
  public Object getAttributeValue()
  {
    return attrValue;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#execute()
   */
  public void applyChange()
  {
    if ( attrValue == null)
      getBoundObject().setAttribute( attrName);
    else
      getBoundObject().setAttribute( attrName, attrValue);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    IModelObject object = getBoundObject();
    String string = (attrName.length() > 0)? attrName: "text()";
    return "set: attribute: "+string+", value: "+attrValue+", object: "+object;
  }
  
  String attrName;
  Object attrValue;
}
