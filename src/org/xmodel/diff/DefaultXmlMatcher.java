/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * DefaultXmlMatcher.java
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xmodel.ChangeSet;
import org.xmodel.IChangeSet;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xml.XmlIO;


/**
 * An implementation of IXmlMatcher which provides basic matching semantics that are appropriate 
 * for documents which make use of <i>id</i> attributes. The use of <i>id</i> attributes is one
 * simple way to <i>establish the identity of an element independent of its content</i>.
 * @see IXmlMatcher
 * <p>
 * This class also provides methods for defining which attributes and elements should be ignored
 * by the diff without having to explicitly check the type of the attribute or element in the
 * <code>shouldDiff</code> methods.
 */
public class DefaultXmlMatcher implements IXmlMatcher
{
  /**
   * Create a DefaultXmlMatcher which will perform an unordered diff.
   */
  public DefaultXmlMatcher()
  {
    this( false);    
  }
  
  /**
   * Create a DefaultXmlMatcher which will perform either an ordered or unordered diff by default.
   * @param order True if the diff should be ordered for all elements.
   */
  public DefaultXmlMatcher( boolean order)
  {
    this.order = order;
    this.ignoreAttributeSet = new HashSet<String>();
    this.ignoreElementSet = new HashSet<String>();
  }
  
  /**
   * Specify whether the children of all elements should be treated as an ordered list.
   * @param order True if children should be treated as ordered list.
   */
  public void setOrderAll( boolean order)
  {
    this.order = order;
  }
  
  /**
   * Add the specified attribute to the list of attributes that should be ignored.
   * @param name The name of the attribute.
   */
  public void ignoreAttribute( String name)
  {
    ignoreAttributeSet.add( name);
  }
  
  /**
   * Add the specified attributes to the list of attributes that should be ignored.
   * @param names The names of the attributes.
   */
  public void ignoreAttributes( String[] names)
  {
    for( int i=0; i<names.length; i++) ignoreAttributeSet.add( names[ i]);
  }
  
  /**
   * Remove the specified attribute from the list of attributes which should be ignored.
   * This method cancels a previous call to the <code>ignoreAttribute</code> method.
   * @param name The name of the attribute.
   */
  public void regardAttribute( String name)
  {
    ignoreAttributeSet.remove( name);
  }
  
  /**
   * Add the specified element to the list of elements which should be ignored.
   * @param name The name of the element.
   */
  public void ignoreElement( String name)
  {
    ignoreElementSet.add( name);
  }
  
  /**
   * Add the specified elements to the list of elements that should be ignored.
   * @param names The names of the elements.
   */
  public void ignoreElements( String[] names)
  {
    for( int i=0; i<names.length; i++) ignoreElementSet.add( names[ i]);
  }
  
  /**
   * Remove the specified element from the list of elements which should be ignored.
   * This method cancels a previous call to the <code>ignoreElement</code> method.
   * @param name The name of the element.
   */
  public void regardElement( String name)
  {
    ignoreElementSet.remove( name);
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
    return !ignoreAttributeSet.contains( attrName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#shouldDiff(org.xmodel.IModelObject, boolean)
   */
  public boolean shouldDiff( IModelObject object, boolean lhs)
  {
    return !ignoreElementSet.contains( object.getType());
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.nu.IXmlMatcher#isList(org.xmodel.IModelObject)
   */
  public boolean isList( IModelObject parent)
  {
    return order;
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.nu.IXmlMatcher#findMatch(java.util.List, org.xmodel.IModelObject)
   */
  public int findMatch( List<IModelObject> children, IModelObject child)
  {
    String type = child.getType();
    String name = Xlate.get( child, "id", "");
    if ( name.length() == 0)
    {
      if ( child.getNumberOfChildren() == 0)
      {
        return findSimpleMatch( children, child);
      }
      else 
      {
        List<IModelObject> siblings = child.getParent().getChildren( type);
        if ( siblings.size() == 1) return findUniqueMatch( children, child);
      }
    }
    else
    {
      for( int i=0; i<children.size(); i++)
      {
        IModelObject candidate = children.get( i);
        if ( candidate == child) return i;
        if ( candidate.isType( type) && candidate.getAttribute( "id").equals( name)) return i;
      }
    }
    return -1;
  }

  /**
   * Find a simple match for the specified child (which must be simple).
   * @param children The children to search.
   * @param child The simple child.
   * @return Returns the index of a simple match for the child or -1.
   */
  protected int findSimpleMatch( List<IModelObject> children, IModelObject child)
  {
    int complexMatch = -1;
    String type = child.getType();
    for( int i=0; i<children.size(); i++)
    {
      IModelObject candidate = children.get( i);
      if ( candidate == child) return i;
      
      if ( candidate.isType( type))
      {
        if ( candidate.getNumberOfChildren() == 0) 
          return i; 
        else 
          complexMatch = i;
      }
    }
    return complexMatch;
  }
  
  /**
   * Find a unique match for the specified child (which must be unique).
   * @param children The children to search.
   * @param child The unique child.
   * @return Returns the index of a unique match for the child or -1.
   */
  protected int findUniqueMatch( List<IModelObject> children, IModelObject child)
  {
    String type = child.getType();
    if ( children.size() > 0)
    {
      IModelObject parent = children.get( 0).getParent();
      List<IModelObject> candidates = parent.getChildren( type);
      if ( candidates.size() == 1) return children.indexOf( candidates.get( 0));
    }
    return -1;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.diff.ICorrelation#isMatch(org.xmodel.IModelObject, 
   * org.xmodel.IModelObject)
   */
  public boolean isMatch( IModelObject localChild, IModelObject foreignChild)
  {
    Object localID = localChild.getAttribute( "id");
    Object foreignID = foreignChild.getAttribute( "id");
    if ( localID == null && foreignID == null)
    {
      return localChild.isType( foreignChild.getType());
    }
    else
    {
      // check type and id
      return (localChild.isType( foreignChild.getType())) && (localID.equals( foreignID));
    }
  }
  
  private Set<String> ignoreAttributeSet;
  private Set<String> ignoreElementSet;
  private boolean order;

  //
  // Tests
  //
  public static void main( String[] args) throws Exception
  {
    ChangeSet changeSet = new ChangeSet();
    XmlIO xmlIO = new XmlIO();
    XmlDiffer differ = new XmlDiffer();
    
    // set of simple elements
    String s1 = 
      "<r>" +
      "  <e>1</e>" +
      "  <e>2</e>" +
      "  <e>3</e>" +
      "</r>";
    
    String s2 = 
      "<r>" +
      "  <e>1</e>" +
      "  <e>3</e>" +
      "  <e>2</e>" +
      "</r>";
    
    IModelObject d1 = xmlIO.read( s1);
    IModelObject d2 = xmlIO.read( s2);
    changeSet.clearChanges();
    differ.diff( d1, d2, changeSet);
    System.out.printf( "set of simple elements:\n%s\n\n", changeSet);

    // set of complex elements with ids
    s1 = 
      "<r>" +
      "  <e id='1'><c/></e>" +
      "  <e id='2'><d/></e>" +
      "  <e id='3'><e/></e>" +
      "</r>";
    
    s2 = 
      "<r>" +
      "  <e id='1'><c/></e>" +
      "  <e id='3'><d/></e>" +
      "  <e id='2'><e/></e>" +
      "</r>";
    
    d1 = xmlIO.read( s1);
    d2 = xmlIO.read( s2);
    changeSet.clearChanges();
    differ.diff( d1, d2, changeSet);
    System.out.printf( "set of complex elements with ids:\n%s\n\n", changeSet);
    
    // lists
    DefaultXmlMatcher matcher = new DefaultXmlMatcher() {
      public boolean isList( IModelObject parent)
      {
        return parent.isType( "r");
      }
    };
    differ.setMatcher( matcher);

    // list of simple elements
    s1 = 
      "<r>" +
      "  <e>1</e>" +
      "  <e>2</e>" +
      "  <e>3</e>" +
      "</r>";
    
    s2 = 
      "<r>" +
      "  <e>1</e>" +
      "  <e>3</e>" +
      "  <e>2</e>" +
      "</r>";
    
    d1 = xmlIO.read( s1);
    d2 = xmlIO.read( s2);
    changeSet.clearChanges();
    differ.diff( d1, d2, changeSet);
    System.out.printf( "list of simple elements:\n%s\n\n", changeSet);

    // list of complex elements with ids
    s1 = 
      "<r>" +
      "  <e id='1'><c/></e>" +
      "  <e id='2'><c/></e>" +
      "  <e id='3'><c/></e>" +
      "</r>";
    
    s2 = 
      "<r>" +
      "  <e id='1'><c/></e>" +
      "  <e id='3'><c/></e>" +
      "  <e id='2'><c/></e>" +
      "</r>";
    
    d1 = xmlIO.read( s1);
    d2 = xmlIO.read( s2);
    changeSet.clearChanges();
    differ.diff( d1, d2, changeSet);
    System.out.printf( "list of complex elements with ids:\n%s\n\n", changeSet);
  }
}
