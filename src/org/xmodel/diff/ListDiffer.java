/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An implementation of IListDiffer that creates a list of change records to be processed
 * after the diff algorithm completes. Two types of change records are created: inserts
 * and deletes. An insert record will have an lIndex >= 0, while a delete record will
 * have an lIndex < 0.
 */
@SuppressWarnings("unchecked")
public class ListDiffer extends AbstractListDiffer
{
  /* (non-Javadoc)
   * @see org.xmodel.diff.IListDiffer#notifyInsert(java.util.List, int, int, java.util.List, int, int)
   */
  public void notifyInsert( List lhs, int lIndex, int lAdjust, List rhs, int rIndex, int count)
  {
    Change change = new Change();
    change.lIndex = lIndex + lAdjust;
    change.rIndex = rIndex;
    change.count = count;
    
    if ( changes == null) changes = new ArrayList<Change>();
    changes.add( change);
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IListDiffer#notifyRemove(java.util.List, int, int, java.util.List, int)
   */
  public void notifyRemove( List lhs, int lIndex, int lAdjust, List rhs, int count)
  {
    Change change = new Change();
    change.lIndex = lIndex + lAdjust;
    change.rIndex = -1;
    change.count = count;
    
    if ( changes == null) changes = new ArrayList<Change>();
    changes.add( change);
  }
  
  /**
   * Returns the change records from the last diff.
   * @return Returns null or the change records from the last diff.
   */
  public List<Change> getChanges()
  {
    if ( changes == null) return Collections.<Change>emptyList();
    return changes;
  }
  
  public static class Change
  {
    public int lIndex;
    public int rIndex;
    public int count;
  }
  
  private List<Change> changes;
}
