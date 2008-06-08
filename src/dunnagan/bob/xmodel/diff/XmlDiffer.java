/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import dunnagan.bob.xmodel.IChangeSet;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IModelObjectFactory;
import dunnagan.bob.xmodel.ModelAlgorithms;

/**
 * An implementation of IXmlDiffer including ordered and unordered differencing algorithms. An
 * IModelObjectFactory is used to clone objects from the right-hand-side tree which need to be added
 * to the left-hand-side tree. If the factory is null by default causing objects will be moved from
 * the right-hand-side tree to the left-hand-side tree instead of cloned.
 * <p>
 * The ordered diff algorithm is designed to preserve original match points between the lhs and rhs
 * lists which helps to produce a minimal set of change records.
 * <p>
 * BUG: The ordered diff algorithm does not check IXmlMatcher.shouldDiff.
 */
@SuppressWarnings("unchecked")
public class XmlDiffer extends AbstractXmlDiffer
{
  public XmlDiffer()
  {
    listDiffer = new XmlListDiffer();
  }
  
  public XmlDiffer( IXmlMatcher matcher)
  {
    this();
    setMatcher( matcher);
  }
  
  public XmlDiffer( IModelObjectFactory factory)
  {
    this();
    setFactory( factory);
  }
  
  /**
   * Set the factory which is used to clone objects from the right-hand tree into the left-hand tree.
   * @param factory The new factory.
   */
  public void setFactory( IModelObjectFactory factory)
  {
    this.factory = factory;
  }
  
  /**
   * Returns the IModelObjectFactory defined on this differ.
   * @return Returns the IModelObjectFactory defined on this differ.
   */
  public IModelObjectFactory getFactory()
  {
    return factory;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.nu.AbstractXmlDiffer#diffSet(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IChangeSet)
   */
  protected boolean diffSet( IModelObject lParent, IModelObject rParent, IChangeSet changeSet)
  {
    IXmlMatcher matcher = getMatcher();
    
    List<IModelObject> rightSet = rParent.getChildren();
    List<IModelObject> rightSetCopy = new ArrayList<IModelObject>( rightSet);
    List<IModelObject> leftSet = lParent.getChildren();
    int recordCount = (changeSet != null)? changeSet.getSize(): 0;
    
    // remove objects that shouldn't be differenced from copy of right set
    ListIterator<IModelObject> rightIter = rightSetCopy.listIterator();
    while( rightIter.hasNext())
    {
      if ( !matcher.shouldDiff( rightIter.next(), false))
        rightIter.remove();
    }
    
    // diff sets
    int removeIndex = 0;
    for( IModelObject left: leftSet)
    {
      if ( matcher.shouldDiff( left, true))
      {
        int matchIndex = matcher.findMatch( rightSetCopy, left);
        if ( matchIndex >= 0)
        {
          IModelObject rightMatch = rightSetCopy.get( matchIndex);
          if ( !diff( left, rightMatch, changeSet) && changeSet == null) return false;
          
          // remove match from right set for (1)
          rightSetCopy.remove( matchIndex);
          
          // update remove index to the next element in the left list
          removeIndex++;
        }
        else
        {
          if ( changeSet != null) changeSet.removeChild( lParent, left, removeIndex); else return false;
        }
      }
    }

    // exit with false if no change set and right set is not empty
    if ( changeSet == null && rightSetCopy.size() > 0) return false;
    
    // (1) add what is left in copy of right set
    for( IModelObject right: rightSetCopy)
      changeSet.addChild( lParent, getFactoryClone( right));
  
    if ( changeSet != null) return changeSet.getSize() == recordCount; else return true;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.nu.AbstractXmlDiffer#diffList(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IChangeSet)
   */
  protected boolean diffList( IModelObject lParent, IModelObject rParent, IChangeSet changeSet)
  {
    if ( changeSet == null) return compareList( lParent, rParent);

    // diff list and see if records were added to change-set
    int oldRecordCount = changeSet.getSize();
    listDiffer.diff( lParent, rParent, changeSet);
    int newRecordCount = changeSet.getSize();
    return newRecordCount == oldRecordCount;
  }

  /**
   * Compare the children of the specified parents.
   * @param lParent The left parent.
   * @param rParent The right parent.
   * @return Returns true if the children are identical.
   */
  protected boolean compareList( IModelObject lParent, IModelObject rParent)
  {
    List<IModelObject> lList = lParent.getChildren();
    List<IModelObject> rList = rParent.getChildren();
    if ( lList.size() != rList.size()) return false;
    
    for( int i=0; i<lList.size(); i++)
    {
      if ( !diff( lList.get( i), rList.get( i), null))
        return false;
    }
    return true;
  }
  
  /**
   * Returns a clone of the specified object using the installed factory. If the factory is null
   * then the object itself is returned.
   * @param object The object to be cloned.
   * @return Returns a clone of the specified object or the object.
   */
  public IModelObject getFactoryClone( IModelObject object)
  {
    if ( factory != null) return ModelAlgorithms.cloneTree( object, factory);
    return object;
  }
  
  private class XmlListDiffer extends AbstractListDiffer
  {
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
    
    /* (non-Javadoc)
     * @see dunnagan.bob.xmodel.diff.IListDiffer#notifyEqual(java.util.List, int, int, java.util.List, int, int)
     */
    public void notifyEqual( final List lhs, int lIndex, int lAdjust, final List rhs, int rIndex, int count)
    {
      IModelObject lParent = this.lParent;
      IModelObject rParent = this.rParent;
      for( int i=0; i<count; i++)
      {
        IModelObject lChild = lParent.getChild( lIndex++);
        IModelObject rChild = rParent.getChild( rIndex++);
        XmlDiffer.this.diff( lChild, rChild, changeSet);
      }
      this.lParent = lParent;
      this.rParent = rParent;
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

    IChangeSet changeSet;
    IModelObject lParent;
    IModelObject rParent;
  };
  
  IModelObjectFactory factory;
  XmlListDiffer listDiffer;
}
