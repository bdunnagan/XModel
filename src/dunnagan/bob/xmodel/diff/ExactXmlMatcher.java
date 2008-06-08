/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.diff;

import java.util.List;

import dunnagan.bob.xmodel.IChangeSet;
import dunnagan.bob.xmodel.IModelObject;

/**
 * An IXmlMatcher which requires that the two elements being matched agree exactly. That is,
 * the type, attributes and value of all elements in the subtree are identical.
 */
public class ExactXmlMatcher implements IXmlMatcher
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IXmlMatcher#startDiff(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IChangeSet)
   */
  public void startDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IXmlMatcher#endDiff(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IChangeSet)
   */
  public void endDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IXmlMatcher#enterDiff(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IChangeSet)
   */
  public void enterDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IXmlMatcher#exitDiff(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IChangeSet)
   */
  public void exitDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IXmlMatcher#findMatch(java.util.List, dunnagan.bob.xmodel.IModelObject)
   */
  public int findMatch( List<IModelObject> children, IModelObject child)
  {
    throw new IllegalStateException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IXmlMatcher#isList(dunnagan.bob.xmodel.IModelObject)
   */
  public boolean isList( IModelObject parent)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IXmlMatcher#isMatch(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject)
   */
  public boolean isMatch( IModelObject leftChild, IModelObject rightChild)
  {
    // compare type
    if ( !leftChild.getType().equals( rightChild.getType())) return false;
    
    // compare value
    Object leftValue = leftChild.getValue();
    Object rightValue = rightChild.getValue();
    if ( leftValue == null || rightValue == null) 
      return leftValue == rightValue; 
    else 
      return leftValue.equals( rightValue);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IXmlMatcher#shouldDiff(dunnagan.bob.xmodel.IModelObject, java.lang.String, boolean)
   */
  public boolean shouldDiff( IModelObject object, String attrName, boolean lhs)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IXmlMatcher#shouldDiff(dunnagan.bob.xmodel.IModelObject, boolean)
   */
  public boolean shouldDiff( IModelObject object, boolean lhs)
  {
    return true;
  }
}
