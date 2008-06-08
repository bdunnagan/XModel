/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;


/**
 * A ChangeSet which produces records which, when applied, result in an intersection operation.
 */
public class IntersectChangeSet extends ChangeSet
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ChangeSet#addChild(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject, int)
   */
  @Override
  public void addChild( IModelObject object, IModelObject child, int index)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ChangeSet#addChild(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject)
   */
  @Override
  public void addChild( IModelObject object, IModelObject child)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ChangeSet#setAttribute(dunnagan.bob.xmodel.IModelObject, java.lang.String, java.lang.Object)
   */
  @Override
  public void setAttribute( IModelObject object, String attrName, Object attrValue)
  {
  }
}
