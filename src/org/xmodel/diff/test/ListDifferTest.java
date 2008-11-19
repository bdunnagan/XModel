/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.diff.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.xmodel.diff.AbstractListDiffer;
import org.xmodel.diff.IListDiffer;

import junit.framework.TestCase;

@SuppressWarnings("unchecked")
public class ListDifferTest extends TestCase
{
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception
  {
    differ = new AbstractListDiffer() {
      public void notifyEqual( final List lhs, int lIndex, int lAdjust, final List rhs, int rIndex, int count)
      {
        if ( stdout) System.out.printf( "  EQL: lIndex=%d, lAdjust=%d, rIndex=%d, count=%d\n", lIndex, lAdjust, rIndex, count);
      }
      public void notifyInsert( final List lhs, int lIndex, int lAdjust, final List rhs, int rIndex, int count)
      {
        if ( stdout) System.out.printf( "  INS: lIndex=%d, lAdjust=%d, rIndex=%d, count=%d\n", lIndex, lAdjust, rIndex, count);
        result.addAll( lIndex+lAdjust, rhs.subList( rIndex, rIndex+count));
      }
      public void notifyRemove( final List lhs, int lIndex, int lAdjust, final List rhs, int count)
      {
        if ( stdout) System.out.printf( "  DEL: lIndex=%d, lAdjust=%d, count=%d\n", lIndex, lAdjust, count);
        for ( int i=0; i<count; i++) result.remove( lIndex+lAdjust);
      }
    };
  }

  public void testPrefab()
  {
    for( int i=0; i<lArray.length; i++)
    {
      lhs = Arrays.asList( lArray[ i]);
      if ( stdout) System.out.println( "lhs: "+lhs);
      
      rhs = Arrays.asList( rArray[ i]);
      if ( stdout) System.out.println( "rhs: "+rhs);
      
      result = new ArrayList( lhs);
      differ.diff( lhs, rhs);
      
      for ( int j=0; j<rhs.size(); j++)
        assertTrue( differ.isMatch( result.get( j), rhs.get( j)));
      
      if ( stdout) System.out.println( "\n");
    }
  }
  
  public void testRandom()
  {
    Integer[] array = new Integer[ 25];
    for ( int i=0; i<25; i++) array[ i] = i;
    
    for( int i=0; i<100; i++)
    {
      lhs = new ArrayList( Arrays.asList( array));
      rhs = new ArrayList( Arrays.asList( array));
      
      if ( stdout) System.out.println( "lhs: "+lhs);
      
      Collections.shuffle( rhs);
      if ( stdout) System.out.println( "rhs: "+rhs);
      
      result = new ArrayList( lhs);
      differ.diff( lhs, rhs);
      
      for ( int j=0; j<rhs.size(); j++)
        assertTrue( differ.isMatch( result.get( j), rhs.get( j)));
      
      if ( stdout) System.out.println( "\n");
    }
  }
  
  static final Integer[] lhs1 = { 0, 1, 2, 3, 4};
  static final Integer[] rhs1 = { 4, 1, 2, 3, 0};

  static final Integer[] lhs2 = { 0, 1, 2, 3};
  static final Integer[] rhs2 = { 0, 1, 2, 3, 4};

  static final Integer[] lhs3 = { 0, 1, 2, 3, 4};
  static final Integer[] rhs3 = { 0, 1, 2, 3};

  static final Integer[] lhs4 = { 0, 1, 2, 3, 4};
  static final Integer[] rhs4 = { 0, 1, 2, 3, 4};

  static final Integer[] lhs5 = { 0, 1, 2, 3, 4};
  static final Integer[] rhs5 = { 4, 3, 2, 1, 0};

  static final Integer[] lhs6 = { 0, 1, 2, 3, 4};
  static final Integer[] rhs6 = { 3, 1, 2, 0, 4};

  static final Integer[] lhs7 = { 0, 3, 2, 1, 4};
  static final Integer[] rhs7 = { 0, 1, 2, 3, 4};

  static final Integer[] lhs8 = { 0, 1, 2, 3, 4};
  static final Integer[] rhs8 = { 3, 1, 4, 0, 2};

  static final Integer[] lhs9 = {};
  static final Integer[] rhs9 = { 0, 1, 2, 3, 4};
  
  static final Integer[] lhs10 = { 0, 1, 2, 3, 4};
  static final Integer[] rhs10 = {};
  
  static final Integer[][] lArray = { lhs1, lhs2, lhs3, lhs4, lhs5, lhs6, lhs7, lhs8, lhs9, lhs10};
  static final Integer[][] rArray = { rhs1, rhs2, rhs3, rhs4, rhs5, rhs6, rhs7, rhs8, rhs9, rhs10};
  
  static final boolean stdout = true;
  
  IListDiffer differ;
  List lhs;
  List rhs;
  List result;
}
