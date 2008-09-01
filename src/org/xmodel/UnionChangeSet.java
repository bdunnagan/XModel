/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel;


/**
 * A ChangeSet which produces records which, when applied, result in a union operation.
 */
public class UnionChangeSet extends ChangeSet
{
  /* (non-Javadoc)
   * @see org.xmodel.ChangeSet#removeAttribute(org.xmodel.IModelObject, java.lang.String)
   */
  @Override
  public void removeAttribute( IModelObject object, String attrName)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.ChangeSet#removeChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  @Override
  public void removeChild( IModelObject object, IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.ChangeSet#removeChild(org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  @Override
  public void removeChild( IModelObject object, IModelObject child)
  {
  }
}
