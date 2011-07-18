/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ListDiffer.java
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
   * @see org.xmodel.diff.AbstractListDiffer#diff(java.util.List, java.util.List)
   */
  @Override
  public void diff( List lhs, List rhs)
  {
    if ( changes != null) changes.clear();
    super.diff( lhs, rhs);
  }

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
