/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IModelObjectFactory;
import dunnagan.bob.xmodel.ModelObjectFactory;
import dunnagan.bob.xmodel.Xlate;

/**
 * An implementation of IXmlConversion which converts JDOM attributes to IModelObject attributes and
 * JDOM children to IModelObject children. This implementation preserves the namespace information.
 * First, each node which has a namespace will retain its namespace prefix.  Second, each node which
 * has a namespace declaration will have an attribute set which contains the fully-qualified namespace.
 * The attribute for a namespace is <b>#ns</b> and for a namespace declaration is <b>#nsdecl</b>.
 * <p>
 * Reference objects are rendered as the objects which they reference.
 * <p>
 * JDOM text nodes are converted to attributes whose attribute name is an empty string. These
 * attributes are understood to be the text node atributes of the object and are therefore treated
 * specially by the X-Path 1.0 parser.
 * <p>
 * Attributes beginning with a hash mark 'xm:' are in the exclude list by default.
 * <p>
 * <b>Warning: attributes declared in a namespace other than the element to which they belong are
 * not handled correctly. The namespace is discarded.</b>
 * @deprecated
 */
@SuppressWarnings("unchecked")
public class NamespaceConversion implements IXmlConversion
{
  public final static String idAttribute = "id";
  public final static String namespaceAttribute = "xm:ns";
  public final static String declarationAttribute = "xm:nd";
  
  /**
   * Create a SimpleExchange which uses ModelObjectFactory to create IModelObject instances.
   */
  public NamespaceConversion()
  {
    this( new ModelObjectFactory());
  }
  
  /**
   * Create a SimpleExchange which uses the specified factory to create IModelObject instances.
   * @param factory The factory to use when creating IModelObject instances.
   */
  public NamespaceConversion( IModelObjectFactory factory)
  {
    this.factory = factory;
    this.includeNamespaces = new ArrayList<String>();
    this.excludeNamespaces = new ArrayList<String>();
    excludeNamespaces.add( "xm");
  }

  /**
   * Add a namespace to be included in the output. If there is at least one namespace prefix in the 
   * include list then namespaces which are not in the include list will automatically be exluded.
   * @param prefix The namespace prefix.
   */
  public void addIncludeNamespace( String prefix)
  {
    includeNamespaces.add( prefix);
  }

  /**
   * Remove a namespace from the list of included namespaces.
   * @param prefix The namespace prefix.
   */
  public void removeIncludeNamespace( String prefix)
  {
    includeNamespaces.remove( prefix);
  }

  /**
   * Add a namespace to be excluded from the output.
   * @param prefix The namespace prefix.
   */
  public void addExcludeNamespace( String prefix)
  {
    excludeNamespaces.add( prefix);
  }
  
  /**
   * Remove a namespace from the list of excluded namespaces.
   * @param prefix The namespace prefix.
   */
  public void removeExcludeNamespace( String prefix)
  {
    excludeNamespaces.remove( prefix);
  }

  /**
   * Returns true if the prefix is not in the list of excludes or appears in the list of includes.
   * @param name The name whose prefix will be examined.
   * @return Returns true if the attribute should be included in the transform.
   */
  private boolean isIncluded( String name)
  {
    int index = name.indexOf( ':');
    if ( index >= 0)
    {
      String prefix = name.substring( 0, index);
      if ( includeNamespaces.contains( prefix)) return true;
      if ( excludeNamespaces.contains( prefix)) return false;
    }
    return true;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlConversion#setFactory(dunnagan.bob.xmodel.IModelObjectFactory)
   */
  public void setFactory( IModelObjectFactory factory)
  {
    this.factory = factory;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlConversion#transform(dunnagan.bob.xmodel.IModelObject, java.util.List)
   */
  public Element transform( IModelObject object, List consumed)
  {
    // filter namespaces
    if ( !isIncluded( object.getType())) return null;
    
    // get type with prefix removed
    String type = removeNamespacePrefix( object.getType());
    
    // create namespaces and namespace declarations
    Namespace namespace = (Namespace)object.getAttribute( namespaceAttribute);
    Element element = (namespace == null)? new Element( type): new Element( type, namespace);
    String id = object.getID();
    if ( id != null && id.length() > 0) element.setAttribute( idAttribute, id);
    
    // copy other information
    copyNamespaceDeclaration( object, element);
    copyAttributes( object, element);
    return element;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlConversion#transform(org.jdom.Element, java.util.List)
   */
  public IModelObject transform( Element element, List consumed)
  {
    // create object
    String objectType = element.getQualifiedName();
    String objectName = getElementID( element);
    IModelObject object = factory.createObject( null, objectType);
    if ( objectName != null) object.setID( objectName);

    // set namespace
    object.setAttribute( namespaceAttribute, element.getNamespace());
    
    // copy other information
    copyNamespaceDeclaration( element, object);
    copyAttributes( element, object);
    return object;
  }
  
  /**
   * Remove the namespace prefix from the specified name.
   * @param name A name with or without a prefix.
   * @return Returns the name with the namespace prefix removed.
   */
  private String removeNamespacePrefix( String name)
  {
    int index = name.indexOf( ':');
    return (index < 0)? name: name.substring( index+1);
  }
  
  /**
   * Find the ID attribute if it exists in any namespace for the specified element.
   * @param element Look for the ID on this element.
   * @return Returns the ID attribute for the specified element.
   */
  private String getElementID( Element element)
  {
    String id = element.getAttributeValue( idAttribute);
    if ( id == null) id = element.getAttributeValue( idAttribute, element.getNamespace());
    return id;
  }
  
  /**
   * Copy the namespace declaration on the specified element to the specified object. If there is no
   * namespace declaration then this method does nothing.
   * @param element The element to copy the namespace declaration from.
   * @param object The object to copy the namespace declaration to.
   */
  protected void copyNamespaceDeclaration( Element element, IModelObject object)
  {
    // add additional namespace declarations
    List declarations = element.getAdditionalNamespaces();
    if ( declarations != null) object.setAttribute( declarationAttribute, declarations);
  }
  
  /**
   * Copy the namespace declaration on the specified object to the specified element. If there is no
   * namespace declaration then this method does nothing.
   * @param object The object to copy the namespace declaration to.
   * @param element The element to copy the namespace declaration from.
   */
  protected void copyNamespaceDeclaration( IModelObject object, Element element)
  {
    List declarations = (List)object.getAttribute( declarationAttribute);
    if ( declarations != null)
    {
      for ( int i=0; i<declarations.size(); i++)
        element.addNamespaceDeclaration( (Namespace)declarations.get( i));
    }
  }
  
  /**
   * Copy the attributes of the specified JDOM element to the specified ModelObject.
   * @param element The source element.
   * @param object The target object.
   */
  protected void copyAttributes( Element element, IModelObject object)
  {
    // copy attributes
    List list = element.getAttributes();
    Iterator iter = list.iterator();
    while( iter.hasNext())
    {
      Attribute attribute = (Attribute)iter.next();
      object.setAttribute( attribute.getName(), attribute.getValue());
    }

    // store text node as object value
    object.setValue( element.getTextTrim());
  }
  
  /**
   * Copy the attributes of the specified IModelObject to the specified JDOM element.
   * TODO: need to support attribute namespaces correctly
   * @param object The source object.
   * @param element The target element.
   */
  protected void copyAttributes( IModelObject object, Element element)
  {
    // set attributes
    Collection attrNames = object.getAttributeNames();
    Iterator iter = attrNames.iterator();
    while( iter.hasNext())
    {
      String attrName = (String)iter.next();
      if ( attrName.length() == 0) continue;
      if ( !isIncluded( attrName)) continue;
      String attrValue = Xlate.get( object, attrName, (String)null);
      if ( attrValue != null) element.setAttribute( removeNamespacePrefix( attrName), attrValue);
    }
    
    // set value
    Object value = object.getValue();
    if ( value != null) element.setText( value.toString());
  }

  IModelObjectFactory factory;
  List<String> includeNamespaces;
  List<String> excludeNamespaces;
}
