/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * XmlDifferTest.java
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
