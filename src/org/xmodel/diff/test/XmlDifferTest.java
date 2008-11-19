/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.diff.test;

import java.net.URL;
import org.xmodel.ChangeSet;
import org.xmodel.IChangeSet;
import org.xmodel.IModelObject;
import org.xmodel.ModelObjectFactory;
import org.xmodel.diff.DefaultXmlMatcher;
import org.xmodel.diff.RegularChangeSet;
import org.xmodel.diff.XmlDiffer;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;

import junit.framework.TestCase;

public class XmlDifferTest extends TestCase
{
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception
  {
    diffSets = new String[ 4];
    for( int i=0; i<diffSets.length; i++)
    {
      diffSets[ i] = "diffSet-"+(i+1)+".xml";
    }
  }

  public void testDiffSet() throws XmlException
  {
    XmlIO xmlIO = new XmlIO();
    
    XmlDiffer differ = new XmlDiffer();
    differ.setFactory( new ModelObjectFactory());

    for( String diffSet: diffSets)
    {
      URL url = getClass().getResource( diffSet);
  
      IModelObject root = xmlIO.read( url);
      IModelObject lhs = root.getFirstChild( "lhs");
      IModelObject rhs = root.getFirstChild( "rhs");
      
      IChangeSet changeSet = new RegularChangeSet();
      assertFalse( differ.diff( lhs, rhs, null));
      
      differ.diff( lhs, rhs, changeSet);
      changeSet.applyChanges();
System.out.printf( "%s:\n%s\n", diffSet, changeSet.toString());
      
      changeSet.clearChanges();
      differ.diff( lhs, rhs, changeSet);
      assertTrue( changeSet.getSize() == 0);
    }
  }
  
  public void testDiffList() throws XmlException
  {
    XmlIO xmlIO = new XmlIO();
    
    XmlDiffer differ = new XmlDiffer();
    differ.setMatcher( new DefaultXmlMatcher( true));
    differ.setFactory( new ModelObjectFactory());

    for( String diffSet: diffSets)
    {
      URL url = getClass().getResource( diffSet);
  
      IModelObject root = xmlIO.read( url);
      IModelObject lhs = root.getFirstChild( "lhs");
      IModelObject rhs = root.getFirstChild( "rhs");
      
      IChangeSet changeSet = new ChangeSet();
      assertFalse( differ.diff( lhs, rhs, null));
      
      differ.diff( lhs, rhs, changeSet);
      changeSet.applyChanges();
System.out.printf( "%s:\n%s\n", diffSet, changeSet.toString());
      
      changeSet.clearChanges();
      differ.diff( lhs, rhs, changeSet);
      assertTrue( changeSet.getSize() == 0);
    }
  }
  
  String[] diffSets;
}
