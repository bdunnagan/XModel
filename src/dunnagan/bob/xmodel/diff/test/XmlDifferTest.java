/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.diff.test;

import java.net.URL;

import junit.framework.TestCase;
import dunnagan.bob.xmodel.ChangeSet;
import dunnagan.bob.xmodel.IChangeSet;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelObjectFactory;
import dunnagan.bob.xmodel.diff.DefaultXmlMatcher;
import dunnagan.bob.xmodel.diff.RegularChangeSet;
import dunnagan.bob.xmodel.diff.XmlDiffer;
import dunnagan.bob.xmodel.xml.XmlException;
import dunnagan.bob.xmodel.xml.XmlIO;

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
