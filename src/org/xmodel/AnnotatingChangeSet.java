/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AnnotatingChangeSet.java
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
package org.xmodel;

import org.xmodel.diff.XmlDiffer;
import org.xmodel.xml.XmlIO;

/**
 * A ChangeSet which annotates the left-hand-side of the diff to produce a document which describes
 * what has been changed. After calling the <code>applyChanges</code> method, the left-hand-side
 * will appear as if it were processed with a UnionChangeSet with appropriate annotations to
 * indicate children that would be inserted and children which would be removed. Annotations are
 * also provided for each attribute that is changed or cleared. Annotations are defined in the
 * <i>diff</i> namespace and are inserted as the first child of the target. Annotations are not
 * defined as processing-instructions because it is more difficult to query the content of a
 * processing-instruction. This means that an annotated document does not conform to its original
 * schema.
 * TODO: Annotations are not documented yet.
 */
public class AnnotatingChangeSet extends ChangeSet
{
  /**
   * Set the factory used to create the annotation elements.
   * @param factory The factory.
   */
  public void setFactory( IModelObjectFactory factory)
  {
    this.factory = factory;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.ChangeSet#applyChanges()
   */
  @Override
  public void applyChanges()
  {
    for( IBoundChangeRecord record: getRecords())
    {
      IModelObject bound = record.getBoundObject();
      switch( record.getType())
      {
        case IChangeRecord.ADD_CHILD:
        {
          IModelObject child = record.getChild();
          IModelObject annotation = factory.createObject( null, "diff:insert");
          child.addChild( annotation, 0);
          record.applyChange();
          break;
        }
        
        case IChangeRecord.REMOVE_CHILD:
        {
          IModelObject child = record.getChild();
          IModelObject annotation = factory.createObject( null, "diff:delete");
          child.addChild( annotation, 0);
          break;
        }
        
        case IChangeRecord.CHANGE_ATTRIBUTE:
        {
          String attrName = record.getAttributeName();
          IModelObject annotation = factory.createObject( null, "diff:change");
          annotation.setAttribute( "attribute", attrName);
          annotation.setAttribute( "from", bound.getAttribute( attrName));
          bound.addChild( annotation, 0);
          record.applyChange();
          break;
        }
        
        case IChangeRecord.CLEAR_ATTRIBUTE:
        {
          String attrName = record.getAttributeName();
          IModelObject annotation = factory.createObject( null, "diff:clear");
          annotation.setAttribute( "attribute", attrName);
          annotation.setAttribute( "from", bound.getAttribute( attrName));
          bound.addChild( annotation, 0);
          record.applyChange();
          break;
        }
      }
    }
  }
  
  private IModelObjectFactory factory = new ModelObjectFactory();
  
  public static void main( String[] args) throws Exception
  {
    XmlIO xmlIO = new XmlIO();
    
    String xml1 = 
      "<a>" +
      "  <b id='A1'>T1</b>" +
      "  <b id='A2'>T2</b>" +
      "  <b id='A3' x='true'>T3</b>" +
      "</a>";

    String xml2 = 
      "<a>" +
      "  <b id='A1' x='true'>T2</b>" +
      "  <b id='A3'>T3</b>" +
      "  <b id='A4'>T4</b>" +
      "</a>";
    
    IModelObject r1 = xmlIO.read( xml1);
    IModelObject r2 = xmlIO.read( xml2);
    
    XmlDiffer differ = new XmlDiffer();
    AnnotatingChangeSet changeSet = new AnnotatingChangeSet();
    differ.diff( r1, r2, changeSet);
    changeSet.applyChanges();
    
    System.out.println( xmlIO.write( r1));
  }
}
