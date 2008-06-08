/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.diff;

import java.util.List;

/**
 * An interface for an algorithm that attempts to find a minimal set of operations on one list so
 * that it is equivalent to another list. There are three callback methods:
 * <ul>
 * <li><code>notifyEqual</code>
 * <li><code>notifyInsert</code>
 * <li><code>notifyRemove</code>
 * </ul>
 * The algorithm must keep track of the index into the lhs list for the child being compared, and an
 * adjustment value which compensates for inserts and deletes which will be performed on the lhs
 * list. Note that the lhs list should not be modified from within any of these callback methods.
 * Instead, the indices of the objects being removed should be stored and processed later. The
 * stored index should usually take into consideration the adjustment value (lIndex + lAdjust).
 */
@SuppressWarnings("unchecked")
public interface IListDiffer
{
  /**
   * Perform the differencing algorithm on the specified lists. The difference is called so that
   * the lhs list will be equivalent to the rhs list.
   * @param lhs The left-hand-side list.
   * @param rhs The right-hand-side list.
   */
  public void diff( List lhs, List rhs);

  /**
   * Returns true if the specified objects match.
   * @param lObject The left-hand-side object.
   * @param rObject The right-hand-side object.
   * @return Returns true if the specified objects match.
   */
  public boolean isMatch( Object lObject, Object rObject);
  
  /**
   * Called when a run of objects from the lhs and rhs lists is equal.
   * @param lhs The left-hand-side list.
   * @param lIndex The index of the object in the lhs list.
   * @param lAdjust The adjustment to lIndex to compensate for previous inserts or deletes.
   * @param rhs The right-hand-side list.
   * @param rIndex The index of the object in the rhs list.
   * @param count The number of objects which are equal.
   */
  public void notifyEqual( final List lhs, int lIndex, int lAdjust, final List rhs, int rIndex, int count);
  
  /**
   * Called when a run of objects from the rhs list should be inserted into the lhs list.
   * The lhs list should not be modified until the <code>diff</code> method returns.
   * @param lhs The left-hand-side list.
   * @param lIndex The index of the object in the lhs list.
   * @param lAdjust The adjustment to lIndex to compensate for previous inserts or deletes.
   * @param rhs The right-hand-side list.
   * @param rIndex The index of the object in the rhs list.
   * @param count The number of objects to insert.
   */
  public void notifyInsert( final List lhs, int lIndex, int lAdjust, final List rhs, int rIndex, int count);
  
  /**
   * Called when a run of objects from the lhs list should be removed.
   * The lhs list should not be modified until the <code>diff</code> method returns.
   * @param lhs The left-hand-side list.
   * @param lIndex The index of the object in the lhs list.
   * @param lAdjust The adjustment to lIndex to compensate for previous inserts or deletes.
   * @param rhs The right-hand-side list.
   * @param count The number of objects to removed.
   */
  public void notifyRemove( final List lhs, int lIndex, int lAdjust, final List rhs, int count);
}
