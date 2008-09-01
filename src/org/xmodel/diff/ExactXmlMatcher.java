/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.diff;

import java.util.List;
import org.xmodel.IChangeSet;
import org.xmodel.IModelObject;


/**
 * An IXmlMatcher which requires that the two elements being matched agree exactly. That is,
 * the type, attributes and value of all elements in the subtree are identical.
 */
public class ExactXmlMatcher implements IXmlMatcher
{
  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#startDiff(org.xmodel.IModelObject, org.xmodel.IModelObject, 
   * org.xmodel.IChangeSet)
   */
  public void startDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#endDiff(org.xmodel.IModelObject, org.xmodel.IModelObject, 
   * org.xmodel.IChangeSet)
   */
  public void endDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#enterDiff(org.xmodel.IModelObject, org.xmodel.IModelObject, 
   * org.xmodel.IChangeSet)
   */
  public void enterDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#exitDiff(org.xmodel.IModelObject, org.xmodel.IModelObject, 
   * org.xmodel.IChangeSet)
   */
  public void exitDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#findMatch(java.util.List, org.xmodel.IModelObject)
   */
  public int findMatch( List<IModelObject> children, IModelObject child)
  {
    throw new IllegalStateException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#isList(org.xmodel.IModelObject)
   */
  public boolean isList( IModelObject parent)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#isMatch(org.xmodel.IModelObject, org.xmodel.IModelObject)
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
   * @see org.xmodel.diff.IXmlMatcher#shouldDiff(org.xmodel.IModelObject, java.lang.String, boolean)
   */
  public boolean shouldDiff( IModelObject object, String attrName, boolean lhs)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#shouldDiff(org.xmodel.IModelObject, boolean)
   */
  public boolean shouldDiff( IModelObject object, boolean lhs)
  {
    return true;
  }
}
