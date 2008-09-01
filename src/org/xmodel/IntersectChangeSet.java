/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel;


/**
 * A ChangeSet which produces records which, when applied, result in an intersection operation.
 */
public class IntersectChangeSet extends ChangeSet
{
  /* (non-Javadoc)
   * @see org.xmodel.ChangeSet#addChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  @Override
  public void addChild( IModelObject object, IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.ChangeSet#addChild(org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  @Override
  public void addChild( IModelObject object, IModelObject child)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.ChangeSet#setAttribute(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
   */
  @Override
  public void setAttribute( IModelObject object, String attrName, Object attrValue)
  {
  }
}
