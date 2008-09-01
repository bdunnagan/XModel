/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.diff;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xmodel.*;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;


/**
 */
public class PercentageXmlMatcher implements IXmlMatcher
{
  /**
   * Create a PercentageXmlMatcher which compares entire subtrees by percentage.
   * @param percentage The threshold percentage difference which will prevent correlation.
   */
  public PercentageXmlMatcher( double percentage)
  {
    this.threshold = percentage;
    this.differ = new XmlDiffer();
    this.differ.setMatcher( this);
  }
  
  /**
   * Create a PercentageXmlMatcher which uses the specified IXmlDiffer to calculate differences.
   * Providing the IXmlDiffer allows the client to control how parts of the tree are compared
   * when calculating the percentage difference.
   * @param differ The differ.
   * @param percentage The threshold percentage difference which will prevent correlation.
   */
  public PercentageXmlMatcher( IXmlDiffer differ, double percentage)
  {
    this.threshold = percentage;
    this.differ = differ;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.diff.nu.IXmlMatcher#startDiff(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, org.xmodel.IChangeSet)
   */
  public void startDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.nu.IXmlMatcher#endDiff(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject, org.xmodel.IChangeSet)
   */
  public void endDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#enterDiff(org.xmodel.IModelObject, org.xmodel.IModelObject, 
   * org.xmodel.IChangeSet)
   */
  public void enterDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#exitDiff(org.xmodel.IModelObject, org.xmodel.IModelObject, 
   * org.xmodel.IChangeSet)
   */
  public void exitDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#shouldDiff(org.xmodel.IModelObject, 
   * java.lang.String, boolean)
   */
  public boolean shouldDiff( IModelObject object, String attrName, boolean lhs)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#shouldDiff(org.xmodel.IModelObject, boolean)
   */
  public boolean shouldDiff( IModelObject object, boolean lhs)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.nu.IXmlMatcher#isList(org.xmodel.IModelObject)
   */
  public boolean isList( IModelObject parent)
  {
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.nu.IXmlMatcher#findMatch(java.util.List, org.xmodel.IModelObject)
   */
  public int findMatch( List<IModelObject> children, IModelObject child)
  {
    int index = -1;
    double min = 1.0;
    for( int i=0; i<children.size(); i++)
    {
      IModelObject otherChild = children.get( i);
      if ( otherChild.isType( child.getType()))
      {
        double percentage = calculatePercentageDifference( child, otherChild);
        if ( percentage < threshold && percentage < min) 
        {
          min = percentage;
          index = i;
        }
      }
    }
IPath path = (index>=0)? ModelAlgorithms.createIdentityPath( children.get( index)): null;
System.out.println( "min="+min);    
System.out.println( "  local: "+ModelAlgorithms.createIdentityPath( child));
System.out.println( "  match: "+path);
    return index;
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.ICorrelation#isMatch(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject)
   */
  public boolean isMatch( IModelObject localChild, IModelObject foreignChild)
  {
    double percentage = calculatePercentageDifference( localChild, foreignChild);
    return percentage < threshold;
  }
  
  /**
   * Calculate the percentage difference between the two specified nodes. The percentage difference
   * is calculated by finding the number of leaves which differ and comparing it to the total number
   * of leaves. This does not mean that only leaves are compared. When the roots of two subtrees are
   * found to differ, then all of their leaves are counted as differing.
   * @param lhs The left-hand-side tree.
   * @param rhs The right-hand-side tree.
   * @return Returns the percentage difference.
   */
  protected double calculatePercentageDifference( IModelObject lhs, IModelObject rhs)
  {
    int ltotal = getNumberOfLeaves( lhs);
    int rtotal = getNumberOfLeaves( rhs);
    int total = (ltotal > rtotal)? ltotal: rtotal;
    IChangeSet changeSet = new RegularChangeSet();
    differ.diff( lhs, rhs, changeSet);

    Set<IModelObject> set = new HashSet<IModelObject>();
    double changes = 0;
    List<IBoundChangeRecord> records = changeSet.getRecords();
    for( IBoundChangeRecord record: records)
    {
      if ( set.contains( record.getBoundObject())) continue;
      set.add( record.getBoundObject());
      
      int leaves = 0;
      switch( record.getType())
      {
        case IChangeRecord.ADD_CHILD:
        case IChangeRecord.REMOVE_CHILD:
          leaves = getNumberOfLeaves( record.getChild());
          break;
         
        case IChangeRecord.CLEAR_ATTRIBUTE:
        case IChangeRecord.CHANGE_ATTRIBUTE:
          leaves = 1;
          break;
      }
      changes += leaves;
    }
   
    System.out.println( "%="+(changes / total));
    return changes / total;
  }
  
  /**
   * Calculate the number of leaves in the specified subtree (including attribute nodes).
   * @param root The root of the subtree.
   * @return Returns the number of leaves.
   */
  static protected int getNumberOfLeaves( IModelObject root)
  {
    int count = 0;
    BreadthFirstIterator iter = new BreadthFirstIterator( root);
    while( iter.hasNext())
    {
      IModelObject object = (IModelObject)iter.next();
      if ( object.getNumberOfChildren() == 0) count++;
      int attributes = object.getAttributeNames().size();
      count += attributes;
    }
    return count;
  }
  
  IXmlDiffer differ;
  double threshold;
  
  static public void main( String[] args) throws XmlException, MalformedURLException
  {
    XmlIO xmlIO = new XmlIO();
    File dir = new File( "C:/diff-101205");
    IModelObject lhs = xmlIO.read( (new File( dir, "lhs.xml")).toURL());
    IModelObject rhs = xmlIO.read( (new File( dir, "rhs.xml")).toURL());
   
    XmlDiffer differ = new XmlDiffer();
    differ.setMatcher( new PercentageXmlMatcher( differ, 0.10));
    
    IChangeSet changeSet = new ChangeSet();
    differ.diff( lhs, rhs, changeSet);
    
    System.out.println( "changes="+changeSet);
    System.out.println( "done.");
  }
}
