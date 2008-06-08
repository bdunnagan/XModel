/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.diff;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dunnagan.bob.xmodel.ChangeSet;
import dunnagan.bob.xmodel.IChangeSet;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.xml.XmlIO;

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
   * @see dunnagan.bob.xmodel.diff.nu.IXmlMatcher#startDiff(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IChangeSet)
   */
  public void startDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.nu.IXmlMatcher#endDiff(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IChangeSet)
   */
  public void endDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IXmlMatcher#enterDiff(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IChangeSet)
   */
  public void enterDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IXmlMatcher#exitDiff(dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IChangeSet)
   */
  public void exitDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IXmlMatcher#shouldDiff(dunnagan.bob.xmodel.IModelObject, 
   * java.lang.String, boolean)
   */
  public boolean shouldDiff( IModelObject object, String attrName, boolean lhs)
  {
    return !ignoreAttributeSet.contains( attrName);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IXmlMatcher#shouldDiff(dunnagan.bob.xmodel.IModelObject, boolean)
   */
  public boolean shouldDiff( IModelObject object, boolean lhs)
  {
    return !ignoreElementSet.contains( object.getType());
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.nu.IXmlMatcher#isList(dunnagan.bob.xmodel.IModelObject)
   */
  public boolean isList( IModelObject parent)
  {
    return order;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.nu.IXmlMatcher#findMatch(java.util.List, dunnagan.bob.xmodel.IModelObject)
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
        if ( candidate.isType( type) && candidate.getID().equals( name)) return i;
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
   * @see dunnagan.bob.xmodel.diff.ICorrelation#isMatch(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  public boolean isMatch( IModelObject localChild, IModelObject foreignChild)
  {
    String localName = localChild.getID();
    String foreignName = foreignChild.getID();
    if ( localName.length() == 0 && foreignName.length() == 0)
    {
      return localChild.isType( foreignChild.getType());
    }
    else
    {
      // check type and id
      return (localChild.isType( foreignChild.getType())) && (localName.equals( foreignName));
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
