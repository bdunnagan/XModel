/*
 * Stonewall Networks, Inc.
 *
 *   Project: XModel
 *   Author:  bdunnagan
 *   Date:    Apr 15, 2008
 *
 * Copyright 2008.  Stonewall Networks, Inc.
 */
package dunnagan.bob.xmodel.xml;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

import dunnagan.bob.xmodel.*;
import dunnagan.bob.xmodel.external.IExternalReference;

/**
 * A class for transforming the model of an XML document containing namespace declarations.
 * The class provides methods for defining namespace associations between a namespace prefix
 * and a namespace URL.  The class can then be used to append or remove prefixes from the
 * the tag names in a model according to the namespace declarations present in the model 
 * according to the standard rules of namespacing.
 * TODO: Namespaced attributes are not supported because the IModelObjectFactory needs
 * another method for create attributes :/
 */
public class NamespaceTransform implements IModelObjectFactory
{
  /**
   * While the NamespaceTransform is responsible for creating objects with the correct 
   * namespace prefix, this method determines the factory that will be used to create
   * the objects.
   * @param factory The factory.
   */
  public void setFactory( IModelObjectFactory factory)
  {
    this.factory = factory;
  }
  
  /**
   * Set the default namespace prefix.
   * @param prefix The prefix without the colon.
   */
  public void setDefaultNamespace( String prefix)
  {
    namespaces.put( "", prefix);
  }
  
  /**
   * Add a namespace.
   * @param prefix The namespace prefix with the colon.
   * @param url The namespace url.
   */
  public void addNamespace( String prefix, String url)
  {
    namespaces.put( url, prefix);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObjectFactory#createClone(dunnagan.bob.xmodel.IModelObject)
   */
  public IModelObject createClone( IModelObject object)
  {
    String prefix = getNamespacePrefix( object.getType(), object);
    if ( prefix == null) throw new IllegalArgumentException( "Object namespace not found: "+object);

    String type = createTypeString( prefix, object.getType());
    IModelObject clone = factory.createObject( null, type);
    
    ModelAlgorithms.copyAttributes( object, clone);
    return clone;
  }

  /**
   * Create a new object.
   * @param parent The non-null locus in a namespaced document.
   * @param type The type of the new object.
   * @return Returns a new object with the correct namespace prefix.
   */
  public IModelObject createObject( IModelObject parent, String type)
  {
    String prefix = getNamespacePrefix( type, parent);
    if ( prefix == null) throw new IllegalArgumentException( "Object namespace not found: "+parent);

    type = createTypeString( prefix, type);
    IModelObject object = factory.createObject( parent, type);
    return object;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObjectFactory#createObject(dunnagan.bob.xmodel.IModelObject, org.xml.sax.Attributes, java.lang.String)
   */
  public IModelObject createObject( IModelObject parent, Attributes attributes, String type)
  {
    String prefix = getNamespacePrefix( type, parent);
    if ( prefix == null) prefix = getNamespacePrefix( type, attributes);
    if ( prefix == null) throw new IllegalArgumentException( "Object namespace not found: "+parent);

    type = createTypeString( prefix, type);
    IModelObject object = factory.createObject( parent, type);
    return object;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelObjectFactory#createExternalObject(dunnagan.bob.xmodel.IModelObject, java.lang.String)
   */
  public IExternalReference createExternalObject( IModelObject parent, String type)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the namespace prefix appropriate by scanning the specified attributes.
   * @param type The untransformed element name.
   * @param attributes The attributes.
   * @return Returns the namespace prefix.
   */
  private String getNamespacePrefix( String type, Attributes attributes)
  {
    String prefix = getPrefix( type);
    String url = findNamespaceDeclaration( prefix, attributes);
    return (url != null)? namespaces.get( url): null;
  }
  
  /**
   * Returns the namespace prefix appropriate for the specified locus in the document.
   * @param type The untransformed element name.
   * @param locus The locus of the element in the document.
   * @return Returns the namespace prefix.
   */
  private String getNamespacePrefix( String type, IModelObject locus)
  {
    String prefix = getPrefix( type);
    String url = findNamespaceDeclaration( prefix, locus);
    return (url != null)? namespaces.get( url): null;
  }
  
  /**
   * Returns the prefix off the specified element name.
   * @param type The element name.
   * @return Returns the prefix, which may be "".
   */
  private String getPrefix( String type)
  {
    int index = type.indexOf( ":");
    return (index >= 0)? type.substring( 0, index): "";
  }
  
  /**
   * Find the namespace declaration for the specified prefix in the specified attributes.
   * @param prefix The prefix.
   * @param attributes The attributes.
   * @return Returns the url of the namespace declaration.
   */
  private String findNamespaceDeclaration( String prefix, Attributes attributes)
  {
    for( int i=0; i<attributes.getLength(); i++)
    {
      String attrName = attributes.getQName( i);
      if ( attrName.startsWith( "xmlns"))
      {
        String thisPrefix = (attrName.length() > 6)? attrName.substring( 6): "";
        if ( thisPrefix.equals( prefix)) return attributes.getValue( i);
      }
    }
    return null;
  }
  
  /**
   * Find the namespace declaration for the specified prefix.
   * @param prefix The prefix.
   * @param locus The locus.
   * @return Returns the url of the namespace declaration.
   */
  private String findNamespaceDeclaration( String prefix, IModelObject locus)
  {
    IModelObject ancestor = locus;
    while( ancestor != null)
    {
      for( String attrName: ancestor.getAttributeNames())
      {
        if ( attrName.startsWith( "xmlns"))
        {
          String thisPrefix = (attrName.length() > 6)? attrName.substring( 6): "";
          if ( thisPrefix.equals( prefix)) return Xlate.get( ancestor, attrName, "");
        }
      }
      ancestor = ancestor.getParent();
    }
    return null;
  }
  
  /**
   * Replaces the prefix of the specified type string with the specified prefix.
   * @param prefix The new prefix.
   * @param type The type string.
   * @return Returns the replaced type string.
   */
  private String createTypeString( String prefix, String type)
  {
    builder.setLength( 0);
    if ( prefix.length() > 0)
    {
      builder.append( prefix);
      builder.append( ':');
    }
    int index = type.indexOf( ":");
    builder.append( (index >= 0)? type.substring( index+1): type);
    return builder.toString();
  }

  private Map<String, String> namespaces = new HashMap<String, String>();
  private IModelObjectFactory factory = new ModelObjectFactory();
  private StringBuilder builder = new StringBuilder();
  
  public static void main( String[] args) throws Exception
  {
    String doc =
      "<xx:root xmlns:xx='http://www.xx.com'>" +
      "  <xx:tag>" +
      "    <yy:tag xmlns:yy='http://www.yy.com'>" +
      "      <yy:tag/>" +
      "    </yy:tag>" +
      "  </xx:tag>" +
      "</xx:root>";
    
    NamespaceTransform factory = new NamespaceTransform();
    factory.addNamespace( "aa", "http://www.xx.com");
    factory.addNamespace( "bb", "http://www.yy.com");
    
    XmlIO xmlIO = new XmlIO();
    xmlIO.setFactory( factory);
    IModelObject root = xmlIO.read( doc);
    //IModelObject clone = ModelAlgorithms.cloneTree( root, factory);
    
    System.out.println( xmlIO.write( root));
  }
}
