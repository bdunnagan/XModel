/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.diff;

import java.util.List;

import dunnagan.bob.xmodel.IChangeSet;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IModelObjectFactory;
import dunnagan.bob.xmodel.ModelAlgorithms;

/**
 * An implementation of AbstractListDiffer which creates records in an IChangeSet.
 */
@SuppressWarnings("unchecked")
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
  public void setFactory( IModelObjectFactory factory)
  {
    this.factory = factory;
  }
  
  /**
   * Diff the children of the specified parents as lists and append to the specified change set.
   * @param lParent The left-hand-side parent.
   * @param rParent The right-hand-side parent.
   * @param changeSet The change set.
   */
  public void diff( IModelObject lParent, IModelObject rParent, IChangeSet changeSet)
  {
    this.changeSet = changeSet;
    this.lParent = lParent;
    this.rParent = rParent;
    diff( lParent.getChildren(), rParent.getChildren());
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IListDiffer#isMatch(java.lang.Object, java.lang.Object)
   */
  public boolean isMatch( Object lObject, Object rObject)
  {
    return matcher.isMatch( (IModelObject)lObject, (IModelObject)rObject);
  }
  
  /**
   * Clone the specified object if the factory is non-null.
   * @param object The object.
   * @return Returns the object or its clone if the factory is non-null.
   */
  private IModelObject getFactoryClone( IModelObject object)
  {
    if ( factory != null) return ModelAlgorithms.cloneTree( object, factory);
    return object;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IListDiffer#notifyEqual(java.util.List, int, int, java.util.List, int, int)
   */
  public void notifyEqual( final List lhs, int lIndex, int lAdjust, final List rhs, int rIndex, int count)
  {
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IListDiffer#notifyInsert(java.util.List, int, int, java.util.List, int, int)
   */
  public void notifyInsert( final List lhs, int lIndex, int lAdjust, final List rhs, int rIndex, int count)
  {
    for( int i=0; i<count; i++)
    {
      IModelObject clone = getFactoryClone( rParent.getChild( rIndex+i));
      changeSet.addChild( lParent, clone, rIndex+i);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IListDiffer#notifyRemove(java.util.List, int, int, java.util.List, int)
   */
  public void notifyRemove( final List lhs, int lIndex, int lAdjust, final List rhs, int count)
  {
    for( int i=0; i<count; i++)
    {
      changeSet.removeChild( lParent, lParent.getChild( lIndex+i), lIndex+lAdjust);
    }
  }

  IModelObjectFactory factory;
  IXmlMatcher matcher = new DefaultXmlMatcher();
  IChangeSet changeSet;
  IModelObject lParent;
  IModelObject rParent;
}
