/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;


/**
 * A ChangeSet which produces records which, when applied, result in a union operation.
 */
public class UnionChangeSet extends ChangeSet
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ChangeSet#removeAttribute(dunnagan.bob.xmodel.IModelObject, java.lang.String)
   */
  @Override
  public void removeAttribute( IModelObject object, String attrName)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ChangeSet#removeChild(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject, int)
   */
  @Override
  public void removeChild( IModelObject object, IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ChangeSet#removeChild(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject)
   */
  @Override
  public void removeChild( IModelObject object, IModelObject child)
  {
  }
}
