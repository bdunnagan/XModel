/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ShallowDiffer.java
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

import java.util.List;
import org.xmodel.IChangeSet;
import org.xmodel.INode;
import org.xmodel.INodeFactory;
import org.xmodel.ModelAlgorithms;

/**
 * An implementation of AbstractListDiffer which creates records in an IChangeSet.
 */
public class ShallowDiffer extends AbstractListDiffer
{
  /**
   * Set the matcher (see IXmlMatcher for details).
   * @param matcher The matcher.
   */
  public void setMatcher( IXmlMatcher matcher)
  {
    this.matcher = matcher;
  }
  
  /**
   * Set the factory used to create objects for the change set.  By default, objects are 
   * moved from the right-hand list to the left-hand list when the change set is applied.
   * @param factory The factory.
   */
  public void setFactory( INodeFactory factory)
  {
    this.factory = factory;
  }
  
  /**
   * Diff the children of the specified parents as lists and append to the specified change set.
   * @param lParent The left-hand-side parent.
   * @param rParent The right-hand-side parent.
   * @param changeSet The change set.
   */
  public void diff( INode lParent, INode rParent, IChangeSet changeSet)
  {
    this.changeSet = changeSet;
    this.lParent = lParent;
    this.rParent = rParent;
    diff( lParent.getChildren(), rParent.getChildren());
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.diff.IListDiffer#isMatch(java.lang.Object, java.lang.Object)
   */
  public boolean isMatch( Object lObject, Object rObject)
  {
    return matcher.isMatch( (INode)lObject, (INode)rObject);
  }
  
  /**
   * Clone the specified object if the factory is non-null.
   * @param object The object.
   * @return Returns the object or its clone if the factory is non-null.
   */
  private INode getFactoryClone( INode object)
  {
    if ( factory != null) return ModelAlgorithms.cloneTree( object, factory);
    return object;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.diff.IListDiffer#notifyEqual(java.util.List, int, int, java.util.List, int, int)
   */
  public void notifyEqual( final List<?> lhs, int lIndex, int lAdjust, final List<?> rhs, int rIndex, int count)
  {
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.diff.IListDiffer#notifyInsert(java.util.List, int, int, java.util.List, int, int)
   */
  public void notifyInsert( final List<?> lhs, int lIndex, int lAdjust, final List<?> rhs, int rIndex, int count)
  {
    for( int i=0; i<count; i++)
    {
      INode clone = getFactoryClone( rParent.getChild( rIndex+i));
      changeSet.addChild( lParent, clone, rIndex+i);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IListDiffer#notifyRemove(java.util.List, int, int, java.util.List, int)
   */
  public void notifyRemove( final List<?> lhs, int lIndex, int lAdjust, final List<?> rhs, int count)
  {
    for( int i=0; i<count; i++)
    {
      changeSet.removeChild( lParent, lParent.getChild( lIndex+i), lIndex+lAdjust);
    }
  }

  INodeFactory factory;
  IXmlMatcher matcher = new DefaultXmlMatcher();
  IChangeSet changeSet;
  INode lParent;
  INode rParent;
}
