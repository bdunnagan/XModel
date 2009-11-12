/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AbstractXmlDiffer.java
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
package org.xmodel.diff;

import java.util.Collection;
import org.xmodel.IChangeSet;
import org.xmodel.IModelObject;


/**
 * An abstract implementation of IXmlDiffer which provides a mechanism for specifying which
 * parts of the subtree should be diff'ed using an ordered diff versus an unordered diff.
 * Parts of the tree which should be diff'ed with an ordered diff are identified by an XPath
 * expression.  Subclasses should call the <code>isList</code> method for each object to
 * determine which algorithm to use.
 */
public abstract class AbstractXmlDiffer implements IXmlDiffer
{
  public AbstractXmlDiffer()
  {
    matcher = new DefaultXmlMatcher();
    depth = 0;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.diff.nu.IXmlDiffer#setMatcher(org.xmodel.diff.nu.IXmlMatcher)
   */
  public void setMatcher( IXmlMatcher matcher)
  {
    this.matcher = matcher;
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.nu.IXmlDiffer#getMatcher()
   */
  public IXmlMatcher getMatcher()
  {
    return matcher;
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.nu.IXmlDiffer#diff(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, org.xmodel.IChangeSet)
   */
  public boolean diff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
    try
    {
      if ( depth++ == 0) matcher.startDiff( lhs, rhs, changeSet);
      matcher.enterDiff( lhs, rhs, changeSet);
      
      if ( changeSet != null)
      {
        int recordCount = changeSet.getSize();
        diffAttributes( lhs, rhs, changeSet);
        internal_diffChildren( lhs, rhs, changeSet);
        return changeSet.getSize() == recordCount;
      }
      else
      {
        if ( !diffAttributes( lhs, rhs, changeSet)) return false;
        if ( !internal_diffChildren( lhs, rhs, changeSet)) return false;
        return true;
      }
    }
    finally
    {
      matcher.exitDiff( lhs, rhs, changeSet);
      if ( --depth == 0) matcher.endDiff( lhs, rhs, changeSet);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.nu.IXmlDiffer#diffAndApply(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject)
   */
  public boolean diffAndApply( IModelObject lhs, IModelObject rhs)
  {
    IChangeSet changeSet = new RegularChangeSet();
    boolean result = diff( lhs, rhs, changeSet);
    changeSet.applyChanges();
    return result;
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.nu.IXmlDiffer#diffAttributes(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, org.xmodel.IChangeSet)
   */
  public boolean diffAttributes( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
    int recordCount = (changeSet != null)? changeSet.getSize(): 0;
    
    // find attributes that need to be removed from lhs
    if ( matcher.shouldDiff( lhs, (String)null, true))
    {
      Collection<String> lhsAttrNames = lhs.getAttributeNames();
      for( String attrName: lhsAttrNames)
      {
        if ( !matcher.shouldDiff( lhs, attrName, true)) continue;
        Object attrValue = rhs.getAttribute( attrName);
        if ( attrValue == null)
        {  
          if ( changeSet != null)
            changeSet.removeAttribute( lhs, attrName);
          else
            return false;
        }
        else if ( !lhs.getAttribute( attrName).equals( attrValue))
        {
          if ( changeSet != null)
            changeSet.setAttribute( lhs, attrName, attrValue);
          else
            return false;
        }
      }
    }
    
    // find attributes that need to be added to lhs
    if ( matcher.shouldDiff( rhs, (String)null, false))
    {
      Collection<String> rhsAttrNames = rhs.getAttributeNames();
      for( String attrName: rhsAttrNames)
      {
        if ( !matcher.shouldDiff( rhs, attrName, false)) continue;
        Object attrValue = rhs.getAttribute( attrName);
        if ( lhs.getAttribute( attrName) == null)
        {
          if ( changeSet != null)
            changeSet.setAttribute( lhs, attrName, attrValue);
          else 
            return false;
        }
      }
    }
    
    if ( changeSet != null) return changeSet.getSize() == recordCount; else return true;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.diff.nu.IXmlDiffer#diffChildren(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, org.xmodel.IChangeSet)
   */
  public boolean diffChildren( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
    return internal_diffChildren( lhs, rhs, changeSet);
  }
  
  /**
   * Diff only the children. The current set of lists has already been populated.
   * @param lhs The left-hand-side tree.
   * @param rhs The right-hand-side tree.
   * @param changeSet The change set.
   * @return Returns true if the children differ.
   */
  protected boolean internal_diffChildren( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
    int recordCount = (changeSet != null)? changeSet.getSize(): 0;
    if ( matcher.shouldDiff( lhs, true) && matcher.shouldDiff( rhs, false))
    {
      if ( matcher.isList( lhs))
      {
        boolean result = diffList( lhs, rhs, changeSet);
        if ( changeSet == null && !result) return false;
      }
      else
      {
        boolean result = diffSet( lhs, rhs, changeSet);
        if ( changeSet == null && !result) return false;
      }
    }
    if ( changeSet != null) return changeSet.getSize() == recordCount; else return true;
  }

  /**
   * Perform an unordered comparison of the left and right children sets and create a minimal set
   * of change records which, when applied to the parent of the objects in the left set, will make
   * the left set identical to the right set. Note that implementations may use the <code>diff</code>
   * method to determine whether objects are identical. This methods must not call the 
   * <code>diffChildren</code> method. 
   * @param leftParent A node in the left-hand-side tree.
   * @param rightParent A node in the right-hand-side tree.
   * @param changeSet The change set where change records should be created.
   * @return Returns true if lists are identical except for ordering.
   */
  protected abstract boolean diffSet( IModelObject leftParent, IModelObject rightParent, IChangeSet changeSet);

  /**
   * Perform an ordered comparison of the left and right children sets and create a minimal set
   * of change records which, when applied to the parent of the objects in the left set, will make
   * the left set identical to the right set. Note that implementations may use the <code>diff</code>
   * method to determine whether objects are identical. This methods must not call the 
   * <code>diffChildren</code> method.  
   * @param leftParent A node in the left-hand-side tree.
   * @param rightParent A node in the right-hand-side tree.
   * @param changeSet The change set where change records should be created.
   * @return Returns true if lists are identical.
   */
  protected abstract boolean diffList( IModelObject leftParent, IModelObject rightParent, IChangeSet changeSet);

  protected IXmlMatcher matcher;
  private int depth;
}
