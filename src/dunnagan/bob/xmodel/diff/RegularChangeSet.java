/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.diff;

import java.util.Collections;
import java.util.Comparator;

import dunnagan.bob.xmodel.ChangeSet;
import dunnagan.bob.xmodel.IBoundChangeRecord;

/**
 * A ChangeSet which organizes its records to provide listener updates that adhere to the following rules:
 * <ul>
 * <li>All attributes are applied to new elements before the elements are parented.
 * <li>All remove operations are processed before add operations.
 * </ul>
 */
public class RegularChangeSet extends ChangeSet
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ChangeSet#applyChanges()
   */
  @Override
  public void applyChanges()
  {
    if ( records == null) return;
    Collections.sort( records, comparator);
    super.applyChanges();
  }
  
  private Comparator<IBoundChangeRecord> comparator = new Comparator<IBoundChangeRecord>() {
    public int compare( IBoundChangeRecord o1, IBoundChangeRecord o2)
    {
      int t0 = o1.getType();
      int t1 = o2.getType();
      if ( t0 == IBoundChangeRecord.CHANGE_ATTRIBUTE || t0 == IBoundChangeRecord.CLEAR_ATTRIBUTE)
      {
        if ( t1 == IBoundChangeRecord.CHANGE_ATTRIBUTE || t1 == IBoundChangeRecord.CLEAR_ATTRIBUTE) return 0;
        return -1;
      }
      else if ( t1 == IBoundChangeRecord.CHANGE_ATTRIBUTE || t1 == IBoundChangeRecord.CLEAR_ATTRIBUTE)
      {
        return 1;
      }
      else if ( t0 == IBoundChangeRecord.REMOVE_CHILD)
      {
        if ( t1 == IBoundChangeRecord.REMOVE_CHILD) return 0;
        return -1;
      }
      else if ( t1 == IBoundChangeRecord.REMOVE_CHILD)
      {
        return 1;
      }
      return 0;
    }
  };
}
