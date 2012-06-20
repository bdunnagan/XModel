/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AbstractListDiffer.java
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
import java.util.List;

/**
 * An reference implementation of IListDiffer. The algorithm attempts to minimize the number of
 * insert and delete callbacks. The algorithm is summarized below:<br>
 * 
 * 1. Find run of equal items in lists and call the callback for equal items.<br>
 * 2. Find run of items in lhs which are not present in rhs.<br>
 *    Find run of items in rhs which are not present in lhs.<br>
 *    In either case, stop searching if an original match point is found (see below).<br>
 * 3. If an original match point is found then do an equal number of insert and remove callbacks.<br>
 * 4. If an original match point is not found then do the least run of either inserts or removes.<br>
 * <p>
 * An original match point is an index where the lhs and rhs lists have equal items after considering
 * the removal and insertion of previous items into the lhs list. These points are significant since
 * they imply a place in the lists where we might find a run of equal items.
 * <p>
 * (9/8/06) bdunnagan: Added conditions to prevent specious insert/delete at end of list (see below).
 */
public abstract class AbstractListDiffer implements IListDiffer
{
  /* (non-Javadoc)
   * @see org.xmodel.diff.IListDiffer#diff(java.util.List, java.util.List)
   */
  public void diff( List<?> lhs, List<?> rhs)
  {
    List<DeferredInsert> inserts = null;
    
    int lAdjust = 0;
    int lIndex = 0;
    int rIndex = 0;
    int lSize = lhs.size();
    int rSize = rhs.size();
    while( lIndex < lSize && rIndex < rSize)
    {
      int equalCount = findRunOfEqualObjects( lhs, lIndex, rhs, rIndex);
      if ( equalCount > 0)
      {
        notifyEqual( lhs, lIndex, lAdjust, rhs, rIndex, equalCount);
        lIndex += equalCount;
        rIndex += equalCount;
      }

      // search lhs for objects which do not match the object in rhs at rIndex
      int removeCount = findRunMissingFromRight( lhs, lIndex, rhs, rIndex);
      
      // (lIndex + removeCount) < lSize is a special case at the end of the left list
      // where we want to force a search for inserts to prevent an extra insert/delete
      // combination:  A  A
      //               B  X
      //                  B
      if ( (lIndex + removeCount) < lSize && removeCount == 1)
      {
        notifyRemove( lhs, lIndex, lAdjust, rhs, removeCount);
        lAdjust--;
        lIndex += removeCount;
      }
      else if ( removeCount > 0)
      {
        // see if a shorter run of inserts can be found
        int insertCount = findRunMissingFromLeft( lhs, lIndex, rhs, rIndex);
        if ( insertCount > 0 && (insertCount < removeCount || (lIndex + removeCount) == lSize))
        {
          // match is found with fewer inserts, so perform inserts
          if ( inserts == null) inserts = new ArrayList<DeferredInsert>();
          inserts.add( new DeferredInsert( lIndex, lAdjust, rIndex, insertCount));
          rIndex += insertCount;
        }
        else
        {
          // match is found with fewer deletes, so perform deletes
          notifyRemove( lhs, lIndex, lAdjust, rhs, removeCount);
          lAdjust -= removeCount;
          lIndex += removeCount;
        }
      }
      else if ( removeCount < 0)
      {
        // an original match point was found, so perform equal deletes and inserts
        int length = -removeCount;
        notifyRemove( lhs, lIndex, lAdjust, rhs, length);
        if ( inserts == null) inserts = new ArrayList<DeferredInsert>();
        inserts.add( new DeferredInsert( lIndex, lAdjust, rIndex, length));
        lAdjust -= length;
        lIndex += length;
        rIndex += length;
      }
    }
    
    // remove extra records found on the end of lhs
    if ( lIndex < lSize) 
    {
      int count = lSize - lIndex;
      notifyRemove( lhs, lIndex, lAdjust, rhs, count);
      lAdjust -= count;
    }
    
    // notify inserts last, but before final inserts
    if ( inserts != null)
    {
      int dAdjust = 0;
      for( DeferredInsert insert: inserts)
      {
        notifyInsert( lhs, insert.lIndex, insert.lAdjust + dAdjust, rhs, insert.rIndex, insert.count);
        dAdjust += insert.count;
        lAdjust += insert.count;
      }
    }
      
    // add extra records found on the end of rhs
    if ( rIndex < rSize) 
    {
      int count = rSize - rIndex;
      notifyInsert( lhs, lIndex, lAdjust, rhs, rIndex, count);
      lAdjust += count;
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.diff.IListDiffer#isMatch(java.lang.Object, java.lang.Object)
   */
  public boolean isMatch( Object lObject, Object rObject)
  {
    return lObject.equals( rObject);
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IListDiffer#notifyEqual(java.util.List, int, int, java.util.List, int, int)
   */
  public void notifyEqual( final List<?> lhs, int lIndex, int lAdjust, List<?> rhs, int rIndex, int count)
  {
  }

  /**
   * Find a run of equal objects.
   * @param lhs The lhs list.
   * @param lStart The offset into the lhs list.
   * @param rhs The rhs list.
   * @param rStart The offset into the rhs list.
   * @return Returns the length of the run.
   */
  public int findRunOfEqualObjects( List<?> lhs, int lStart, List<?> rhs, int rStart)
  {
    int lSize = lhs.size();
    int rSize = rhs.size();
    int length = 0;
    while( lStart < lSize && rStart < rSize)
    {
      Object lObject = lhs.get( lStart++);
      Object rObject = rhs.get( rStart++);
      if ( !isMatch( lObject, rObject)) return length;
      length++;
    }
    return length;
  }
  
  /**
   * Find a run of objects in the rhs list which is missing from the lhs list. If during the search
   * a match is found between the two lists, then zero minus the length up to the match point is
   * returned.
   * @param lhs The lhs list.
   * @param lStart The offset into the lhs list.
   * @param rhs The rhs list.
   * @param rStart The offset into the rhs list.
   * @return Returns the length of the run or the negated length to the match point.
   */
  public int findRunMissingFromLeft( List<?> lhs, int lStart, List<?> rhs, int rStart)
  {
    int length = 0;
    int lSize = lhs.size();
    int rSize = rhs.size();
    if ( lSize <= lStart) return 0;
    Object lMatch = lhs.get( lStart);
    while( rStart < rSize)
    {
      Object rObject = rhs.get( rStart++);
      if ( lSize <= lStart) return length;
      Object lObject = lhs.get( lStart++);
      if ( isMatch( lObject, rObject)) return -length;
      if ( isMatch( rObject, lMatch)) return length;
      length++;
    }
    return length;
  }

  /**
   * Find a run of objects in the lhs list which is missing from the rhs list. If during the search
   * a match is found between the two lists, then zero minus the length up to the match point is
   * returned.
   * @param lhs The lhs list.
   * @param lStart The offset into the lhs list.
   * @param rhs The rhs list.
   * @param rStart The offset into the rhs list.
   * @return Returns the length of the run or the negated length to the match point.
   */
  public int findRunMissingFromRight( List<?> lhs, int lStart, List<?> rhs, int rStart)
  {
    int length = 0;
    int lSize = lhs.size();
    int rSize = rhs.size();
    if ( rSize <= rStart) return 0;
    Object rMatch = rhs.get( rStart);
    while( lStart < lSize)
    {
      Object lObject = lhs.get( lStart++);
      if ( rSize <= rStart) return length;
      Object rObject = rhs.get( rStart++);
      if ( isMatch( lObject, rObject)) return -length;
      if ( isMatch( lObject, rMatch)) return length;
      length++;
    }
    return length;
  }
  
  /**
   * A structure for holding the information for an insert event.
   */
  final class DeferredInsert
  {
    DeferredInsert( int lIndex, int lAdjust, int rIndex, int count)
    {
      this.lIndex = lIndex;
      this.lAdjust = lAdjust;
      this.rIndex = rIndex;
      this.count = count;
    }
    
    int lIndex;
    int lAdjust;
    int rIndex;
    int count;
  }
  
  public static void main( String[] args) throws Exception
  {
    List<Object> lhs = new ArrayList<Object>();
    List<Object> rhs = new ArrayList<Object>();
//    lhs.add( 'A'); rhs.add( 'A');
//    lhs.add( 'B');
//    lhs.add( 'C'); rhs.add( 'C');
//    lhs.add( 'D'); rhs.add( 'E');
//    lhs.add( 'E');
    
    lhs.add( 'A'); rhs.add( 'A');
                   rhs.add( 'B');
                   rhs.add( 'C');
    lhs.add( 'D'); rhs.add( 'D');                  
                 
    AbstractListDiffer differ = new AbstractListDiffer() {
      public void notifyEqual( List<?> lhs, int lIndex, int lAdjust, List<?> rhs, int rIndex, int count)
      {
        for( int i=0; i<count; i++)
        {
          System.out.println( "EQL: left="+lhs.get( lIndex+i)+", right="+rhs.get( rIndex+i));
        }
      }
      public void notifyInsert( List<?> lhs, int lIndex, int lAdjust, List<?> rhs, int rIndex, int count)
      {
        for( int i=0; i<count; i++)
        {
          System.out.println( "INS: right="+rhs.get( rIndex+i)+", at="+(lIndex+lAdjust+i));
        }
      }
      public void notifyRemove( List<?> lhs, int lIndex, int lAdjust, List<?> rhs, int count)
      {
        for( int i=0; i<count; i++)
        {
          System.out.println( "DEL: left="+lhs.get( lIndex+i)+", at="+(lIndex+lAdjust));
        }
      }
    };
    
    differ.diff( lhs, rhs);
  }
}
