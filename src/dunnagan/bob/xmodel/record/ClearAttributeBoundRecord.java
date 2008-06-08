/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.record;

import dunnagan.bob.xmodel.IBoundChangeRecord;
import dunnagan.bob.xmodel.IChangeRecord;
import dunnagan.bob.xmodel.IModelObject;

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
   * @see dunnagan.bob.xmodel.IBoundChangeRecord#createUndoRecord()
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
   * @see dunnagan.bob.xmodel.IBoundChangeRecord#createUnboundRecord()
   */
  public IChangeRecord createUnboundRecord()
  {
    return new ClearAttributeRecord( getPath(), attrName);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IBoundChangeRecord#createUnboundRecord(dunnagan.bob.xmodel.IModelObject)
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
   * @see dunnagan.bob.xmodel.record.AbstractChangeRecord#getAttributeName()
   */
  @Override
  public String getAttributeName()
  {
    return attrName;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IBoundChangeRecord#execute()
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
