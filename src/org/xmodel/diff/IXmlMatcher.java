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
 * The goal of an IXmlMatcher implementation is to establish the identity of an element independent 
 * of the content of the element so that the element can be recognized when its content differs 
 * between two documents. In addition, the IXmlMatcher implementation must tell the IXmlDiffer
 * implementation whether the children of an element should be considered as an ordered list or
 * as an unordered set. 
 * <p>
 * Only the <code>isMatch</code> method will be called on elements which are being compared
 * as an ordered list. In contrast, only the <code>findMatch</code> method is called on 
 * children being compared as an unordered set.
 */
public interface IXmlMatcher
{
  /**
   * This method is called by the differencing engine when the diff begins.
   * @param lhs The left-hand-side of the diff.
   * @param rhs The right-hand-side of the diff.
   * @param changeSet The change set.
   */
  public void startDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet);
  
  /**
   * This method is called by the differencing engine when the diff is finished.
   * @param lhs The left-hand-side of the diff.
   * @param rhs The right-hand-side of the diff.
   * @param changeSet The change set.
   */
  public void endDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet);
  
  /**
   * This method is called by the differencing engine when the specified lhs and rhs have
   * been determined to be equal and before the differencing of the content of the two 
   * elements has started. Invocations of the <code>enterDiff</code> and
   * <code>exitDiff</code> methods are nested according to the nesting of the elements
   * in the document. 
   * @param lhs The left-hand-side of the diff.
   * @param rhs The right-hand-side of the diff.
   * @param changeSet The change set.
   */
  public void enterDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet);
  
  /**
   * This method is called by the differencing engine when the specified lhs and rhs have
   * been determined to be equal and after the differencing of the content of the two 
   * elements has completed. Invocations of the <code>enterDiff</code> and
   * <code>exitDiff</code> methods are nested according to the nesting of the elements
   * in the document. 
   * @param lhs The left-hand-side of the diff.
   * @param rhs The right-hand-side of the diff.
   * @param changeSet The change set.
   */
  public void exitDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet);
  
  /**
   * Returns true if the specified attribute should be considered by the diff algorithm.
   * The attribute name will be null if all attributes are being queried as with the
   * <code>getAttributeNames</code> method.
   * @param object The object where the attribute is stored.
   * @param attrName The name of the attribute or null.
   * @param lhs True if the node comes from the lhs tree.
   * @return Returns true if the attribute should be diffed.
   */
  public boolean shouldDiff( IModelObject object, String attrName, boolean lhs);
  
  /**
   * Returns true if the specified object should be considered by the diff algorithm. The
   * object may come from the left-hand-side or the right-hand-side of the diff.
   * @param object The object.
   * @param lhs True if the node comes from the lhs tree.
   * @return Returns true if the object should be diffed.
   */
  public boolean shouldDiff( IModelObject object, boolean lhs);
  
  /**
   * Returns true if the difference algorithm should treat the children of the specified 
   * parent as a list. By default, children are differenced as a set.
   * @param parent The parent.
   * @return Returns true if an ordered diff should be used.
   */
  public boolean isList( IModelObject parent);
  
  /**
   * Find a match for the specified child in the specified list.
   * @param children The children to search.
   * @param child The child.
   * @return Returns the index of the match that was found or -1.
   */
  public int findMatch( List<IModelObject> children, IModelObject child);
  
  /**
   * Returns true if the two children correlate.
   * @param leftChild The left-hand-side of the diff.
   * @param rightChild The right-hand-side of the diff.
   * @return Returns true if the two children correlate.
   */
  public boolean isMatch( IModelObject leftChild, IModelObject rightChild);
}
