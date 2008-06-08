/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xsd;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import dunnagan.bob.xmodel.*;
import dunnagan.bob.xmodel.xml.XmlException;
import dunnagan.bob.xmodel.xml.XmlIO;
import dunnagan.bob.xmodel.xpath.PathElement;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.Context;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * A class which provides services related to XSD documents.  The services will only work if all
 * names and references in the schema are qualified with an appropriate namespace prefix, and all
 * import and include declarations have been preprocessed.  The class provides two constructors:
 * one which loads a schema from a URL and performs the aforementioned preprocessing and another
 * which takes a schema root which has already been preprocessed.
 */
public class Xsd
{
  /**
   * Create an Xsd object with the specified schema root.
   * @param url The URL of the schema.
   */
  public Xsd( URL url) throws XmlException
  {
    root = load( url);
  }
  
  /**
   * Create an Xsd object with the specified schema root.  The schema should be complete with
   * all includes and imports resolved and populated under the schema root.
   * @param root The root of the schema.
   */
  public Xsd( IModelObject root)
  {
    this.root = root;
  }

  /**
   * Load the schema from the specified url including all of its includes and imports.
   * @param url The URL.
   * @return Returns the root of the schema with all includes and imports.
   */
  protected IModelObject load( URL url) throws XmlException
  {
    // load root xsd
    XmlIO xmlIO = new XmlIO();
    IModelObject root = xmlIO.read( url);
    if ( !root.isType( "xs:schema"))
    {
      root = null;
      throw new XsdException( "Document does not appear to be an XSD: "+url);
    }
    
    // qualify names
    namespacePrefixExpr.setVariable( "url", Xlate.get( root, "targetNamespace", ""));
    String prefix = namespacePrefixExpr.evaluateString( new Context( root), "");
    if ( prefix.length() > 0) qualifyNames( root, prefix);
    
    // load includes
    List<IModelObject> includes = root.getChildren( "xs:include");
    for( IModelObject decl: includes) loadInclude( root, prefix, decl);
    root.removeChildren( "xs:include");

    // load imports
    List<IModelObject> imports = root.getChildren( "xs:import");
    for( IModelObject decl: imports) loadImport( root, decl);
    root.removeChildren( "xs:import");
    
    return root;
  }
  
  /**
   * Load the schema from the specified url including all of its includes and imports.
   * @param prefix The target namespace prefix.
   * @param url The URL.
   * @return Returns the root of the schema with all includes and imports.
   */
  protected IModelObject load( String prefix, URL url) throws XmlException
  {
    System.out.println( "Loading schema: "+url);
    
    // load root xsd
    XmlIO xmlIO = new XmlIO();
    IModelObject root = xmlIO.read( url);
    if ( !root.isType( "xs:schema"))
    {
      root = null;
      throw new XsdException( "Document does not appear to be an XSD: "+url);
    }
    
    // qualify names
    if ( prefix.length() > 0) qualifyNames( root, prefix);
    
    // load includes
    List<IModelObject> includes = root.getChildren( "xs:include");
    for( IModelObject decl: includes) loadInclude( root, prefix, decl);

    // load imports
    List<IModelObject> imports = root.getChildren( "xs:import");
    for( IModelObject decl: imports) loadImport( root, decl);
    
    return root;
  }
  
  /**
   * Load the schema in the specified include declaration.
   * @param root The destination of the includes.
   * @param prefix The schema prefix.
   * @param decl The include declaration.
   */
  protected void loadInclude( IModelObject root, String prefix, IModelObject decl) throws XmlException
  {
    String spec = Xlate.get( decl, "schemaLocation", "");
    if ( includes == null) includes = new HashSet<String>();
    if ( includes.contains( spec)) return;
    includes.add( spec);
    
    try
    {
      URL url = new URL( spec);
      IModelObject includeRoot = load( prefix, url);
      
      List<IModelObject> simpleTypes = includeRoot.getChildren( "xs:simpleType");
      for( IModelObject simpleType: simpleTypes) root.addChild( simpleType);
      
      List<IModelObject> complexTypes = includeRoot.getChildren( "xs:complexType");
      for( IModelObject complexType: complexTypes) root.addChild( complexType);
      
      List<IModelObject> groups = includeRoot.getChildren( "xs:group");
      for( IModelObject group: groups) root.addChild( group);
      
      List<IModelObject> elements = includeRoot.getChildren( "xs:element");
      for( IModelObject element: elements) root.addChild( element);
    }
    catch( MalformedURLException e)
    {
      throw new XsdException( "Unable to resolve include declaration with url: "+spec, e);
    }
  }
  
  /**
   * Load the schema in the specified import declaration.
   * @param root The destination of the includes.
   * @param decl The import declaration.
   */
  protected void loadImport( IModelObject root, IModelObject decl) throws XmlException
  {
    String spec = Xlate.get( decl, "schemaLocation", "");
    String namespace = Xlate.get( decl, "namespace", "");
    namespacePrefixExpr.setVariable( "url", namespace);
    String prefix = namespacePrefixExpr.evaluateString( new Context( root), "");
    
    String qualifiedSpec = spec+"("+namespace+")";
    if ( imports == null) imports = new HashSet<String>();
    if ( imports.contains( qualifiedSpec)) return;
    imports.add( qualifiedSpec);
    
    try
    {
      URL url = new URL( spec);
      IModelObject importRoot = load( prefix, url);
      
      List<IModelObject> simpleTypes = importRoot.getChildren( "xs:simpleType");
      for( IModelObject simpleType: simpleTypes) root.addChild( simpleType);
      
      List<IModelObject> complexTypes = importRoot.getChildren( "xs:complexType");
      for( IModelObject complexType: complexTypes) root.addChild( complexType);
      
      List<IModelObject> groups = importRoot.getChildren( "xs:group");
      for( IModelObject group: groups) root.addChild( group);
      
      List<IModelObject> elements = importRoot.getChildren( "xs:element");
      for( IModelObject element: elements) root.addChild( element);
    }
    catch( MalformedURLException e)
    {
      throw new XsdException( "Unable to resolve include declaration with url: "+spec, e);
    }
  }
  
  /**
   * Prefix unqualified names and references with the specified prefix.
   * @param root The root of the schema.
   * @param prefix The prefix.
   */
  private void qualifyNames( IModelObject root, String prefix)
  {
    // prepend prefix to all names and references
    BreadthFirstIterator iter = new BreadthFirstIterator( root);
    while( iter.hasNext())
    {
      IModelObject node = (IModelObject)iter.next();
      if ( node.isType( "xs:element") || node.isType( "xs:simpleType") || node.isType( "xs:complexType") | node.isType( "xs:group"))
      {
        String name = Xlate.get( node, "name", (String)null);
        if ( name != null && name.indexOf( ":") < 0) node.setAttribute( "name", prefix+":"+name);
        
        String type = Xlate.get( node, "type", (String)null);
        if ( type != null && type.indexOf( ":") < 0) node.setAttribute( "type", prefix+":"+type);
      }
      else if ( node.isType( "xs:restriction") || node.isType( "xs:extension"))
      {
        String base = Xlate.get( node, "base", (String)null);
        if ( base != null && base.indexOf( ":") < 0) node.setAttribute( "base", prefix+":"+base);
      }
    }
  }

  /**
   * Returns the root of the schema.
   * @return Returns the root of the schema.
   */
  public IModelObject getRoot()
  {
    return root;
  }
  
  /**
   * Returns the prefix of the target namespace for the document.
   * @return Returns the prefix for the target namespace of the document.
   */
  public String getTargetNamespacePrefix()
  {
    namespacePrefixExpr.setVariable( "url", Xlate.get( root, "targetNamespace", ""));
    return namespacePrefixExpr.evaluateString( new Context( root), "");
  }

  /**
   * Returns the type of the element.
   * @param schema The element schema.
   * @return Returns the type of the element.
   */
  static public String getElementType( IModelObject schema)
  {
    return elementTypeStringExpr.evaluateString( new Context( schema), "");
  }
  
  /**
   * Returns the type declaration for the specified element schema or null.
   * @param schema The element schema.
   * @return Returns the type declaration for the specified element schema or null.
   */
  static public IModelObject getTypeDeclaration( IModelObject schema)
  {
    return typeDeclarationExpr.queryFirst( schema);
  }
  
  /**
   * Returns the schema node which defines the simple type of the specified element schema.
   * @param schema The element schema.
   * @return Returns the simple type of the specified element schema.
   */
  static public IModelObject getSimpleType( IModelObject schema)
  {
    return simpleTypeExpr.queryFirst( schema);
  }
  
  /**
   * Returns the enumerations restriction element for the specified element schema.
   * @param schema An element schema.
   * @return Returns the enumerations restriction element for the specified element schema.
   */
  static public IModelObject getEnumerations( IModelObject schema)
  {
    return enumRestrictionExpr.queryFirst( schema);
  }
  
  /**
   * Returns the minimum number of occurences of the specified element.
   * @param schema The element schema.
   * @return Returns the minimum number of occurences of the specified element.
   */
  static public int minOccurences( IModelObject schema)
  {
    return Xlate.get( schema, "minOccurs", 1);
  }
  
  /**
   * Returns the maximum number of occurences of the specified element.  If the occurences
   * is unbounded then -1 is returned.
   * @param schema The element schema.
   * @return Returns the maximum number of occurences of the specified element.
   */
  static public int maxOccurences( IModelObject schema)
  {
    String maxOccurs = Xlate.get( schema, "maxOccurs", "");
    if ( maxOccurs.equals( "unbounded")) return -1;
    return Xlate.get( schema, "maxOccurs", 1);
  }
  
  /**
   * Returns the root of the schema for the specified element.
   * @param element An element corresponding to the specified schema.
   * @return
   */
  public IModelObject getElementSchema( IModelObject element)
  {
    // find first leaf whose parentage matches the elements
    elementSchemaFinder.setVariable( "name", element.getType());
    List<IModelObject> leaves = elementSchemaFinder.query( root, null);
    for( IModelObject leaf: leaves)
      if ( compareBranch( leaf, element))
        return leaf;
    return null;
  }
  
  /**
   * Returns the element schema which corresponds to the specified path.  The path may contain
   * extra elements which come before the matching global element in the schema.
   * @param path The path.
   * @return Returns the element schema which corresponds to the specified path.
   */
  public IModelObject getElementSchema( IPath path)
  {
    IPathElement element = path.getPathElement( path.length() - 1);
    String type = element.type();
    if ( type == null) type = "*";

    // find first leaf whose parentage matches the elements
    elementSchemaFinder.setVariable( "name", type);
    List<IModelObject> leaves = elementSchemaFinder.query( root, null);
    for( IModelObject leaf: leaves)
      if ( compareBranch( leaf, path, path.length()-1))
        return leaf;
    return null;
  }
  
  /**
   * Returns the element schema which corresponds to the specified path relative to the specified
   * context.  The root ancestor of the context argument need not be a global element of the schema.
   * @param context The context.
   * @param path The path.
   * @return Returns the matching element schema.
   */
  public IModelObject getElementSchema( IModelObject context, IPath path)
  {
    if ( path.isAbsolute( null)) return getElementSchema( path);
    
    CanonicalPath identityPath = new CanonicalPath();
    identityPath.addElement( new PathElement( IAxis.CHILD, context.getType()));
    for( int i=0; i < path.length(); i++) 
    {
      IPathElement pathElement = path.getPathElement( i);
      identityPath.addElement( pathElement.clone());
    }
    
    return getElementSchema( identityPath);
  }
  
  /**
   * Returns the schema element of for the parent of the specified element or null.
   * @param schema The element schema.
   * @param element The element.
   * @return Returns the schema element of for the parent of the specified element or null.
   */
  static public IModelObject getElementParentSchema( IModelObject schema, IModelObject element)
  {
    IModelObject parent = element.getParent();
    if ( parent == null) return null;
    
    parentSchemaExpr.setVariable( "name", parent.getType());
    return parentSchemaExpr.queryFirst( schema);
  }
  
  /**
   * Find a global element which is the root schema for the specified element.
   * @param element The element.
   * @return Returns the schema of a global element.
   */
  public IModelObject getRootElementSchema( IModelObject element)
  {
    IModelObject schema = getElementSchema( element);
    if ( schema == null) return null;

    IModelObject ancestor = findElementHead( schema, element);
    if ( ancestor == null) return null;
    
    rootElementSchemaFinder.setVariable( "name", ancestor.getID());
    return rootElementSchemaFinder.queryFirst( root);
  }

  /**
   * Returns the ancestor of the specified element (or self) which corresponds to the global
   * element in the schema to which the specified element belongs.
   * @param element An element to which the schema applies.
   * @return Returns null or an ancestor of the specified element.
   */
  public IModelObject getGlobalElementAncestor( IModelObject element)
  {
    IModelObject schema = getElementSchema( element);
    if ( schema == null) return null;
    return findElementHead( schema, element);
  }
  
  /**
   * Returns the schema qualified path of the specified element. The schema qualified path is an
   * absolute or relative path whose first location step is a root element in the schema which is
   * the ancestor of the schema of the specified element.
   * @param element A data element whose schema is defined in this schema.
   * @param absolute True if the path should be absolute.
   * @return Returns null or the schema qualified path of the specified element.
   */
  public CanonicalPath getElementSchemaPath( IModelObject element, boolean absolute)
  {
    IModelObject ancestor = getGlobalElementAncestor( element);
    if ( ancestor == null) return null;

    CanonicalPath result = ModelAlgorithms.createRelativePath( ancestor, element);
    if ( absolute)
    {
      IPathElement pathElement = result.removeElement( 0);
      result.addElement( 0, new PathElement( IAxis.ROOT, pathElement.type()));
    }
    
    return result;
  }

  /**
   * Create required attributes of the specified element.
   * @param schema The element schema.
   */
  public void createRequiredAttributes( IModelObject schema, IModelObject element)
  {
    List<IModelObject> attributes = requiredAttributeExpr.query( schema, null);
    for( IModelObject attribute: attributes)
    {
      String name = Xlate.get( attribute, "name", (String)null);
      element.setAttribute( name, generateValue( attribute));
    }
  }
  
  /**
   * Generate a valid value for the specified element (or attribute) schema.
   * @param schema The element or attribute schema.
   * @return Returns the value.
   */
  public String generateValue( IModelObject schema)
  {
    String dfault = Xlate.get( schema, "default", (String)null);
    if ( dfault != null) return dfault;
    
    IModelObject typeNode = getSimpleType( schema);
    String type = Xlate.get( typeNode, "");
    if ( type.equals( "xs:string") || type.equals( "xs:token") || type.equals( "xs:normalizedString"))
    {
      IModelObject restriction = getEnumerations( schema);
      if ( restriction == null) return "";
      
      List<IModelObject> enumerations = restriction.getChildren( "xs:enumeration");
      if ( enumerations.size() == 0) return "";
      
      IModelObject enumeration = enumerations.get( 0);
      return Xlate.get( enumeration, "value", "");
    }
    else if ( type.equals( "xs:int") || 
              type.equals( "xs:long") ||
              type.equals( "xs:short") ||
              type.contains( "nteger") || 
              type.contains( "decimal") || 
              type.startsWith( "unsigned"))
    {
      IModelObject restriction = restrictionExpr.queryFirst( schema);
      if ( restriction == null) return null;

      int value = 0;
      IModelObject maxInclusive = restriction.getFirstChild( "xs:maxInclusive");
      if ( maxInclusive != null) value = Xlate.get( maxInclusive, "value", 0);
      
      IModelObject maxExclusive = restriction.getFirstChild( "xs:maxExclusive");
      if ( maxExclusive != null) value = Xlate.get( maxExclusive, "value", 0);

      IModelObject minInclusive = restriction.getFirstChild( "xs:minInclusive");
      if ( minInclusive != null) value = Xlate.get( minInclusive, "value", 0);
      
      IModelObject minExclusive = restriction.getFirstChild( "xs:minExclusive");
      if ( minExclusive != null) value = Xlate.get( minExclusive, "value", 0);
      
      return Integer.toString( value);
    }
    
    return "";
  }
  
  /**
   * Returns the ancestor of the specified element which matches the schema of a global element.
   * @param schema The schema of the element.
   * @param element The element.
   * @return Returns null or an ancestor of the specified element.
   */
  private IModelObject findElementHead( IModelObject schema, IModelObject element)
  {
    parentSchemaExpr.setVariable( "name", element.getType());
    List<IModelObject> candidates = parentSchemaExpr.query( schema, null);
    if ( candidates.size() == 0) return element;

    element = element.getParent();
    if ( element == null) return null;
    
    for ( IModelObject candidate: candidates)
    {
      IModelObject ancestor = findElementHead( candidate, element.getParent());
      if ( ancestor != null) return ancestor;
    }
    
    return null;
  }
  
  /**
   * Returns true if the specified schema is the schema of the specified element. This method assumes 
   * that the schema argument name matches the element argument name and begins the search with the parents 
   * of the specified arguments.
   * @param schema The schema.
   * @param element The element.
   * @return Returns true if the specified schema is the schema of the specified element.
   */
  private boolean compareBranch( IModelObject schema, IModelObject element)
  {
    parentSchemaExpr.setVariable( "name", element.getType());
    List<IModelObject> candidates = parentSchemaExpr.query( schema, null);
    if ( candidates.size() == 0) return true;
    
    element = element.getParent();
    if ( element == null) return false;
    
    for ( IModelObject candidate: candidates)
    {
      if ( compareBranch( candidate, element.getParent()))
        return true;
    }
    
    return false;
  }

  /**
   * Returns true if the path of the specified schema matches the path segment which ends with the 
   * specified path element index.  The schema argument must match the type of the path element at
   * the specified index.
   * @param schema The schema.
   * @param path The path.
   * @param index The path index.
   * @return Returns true if the schema matches the path segment.
   */
  private boolean compareBranch( IModelObject schema, IPath path, int index)
  {
    index--;
    if ( index < 0) return true;
    
    IPathElement element = path.getPathElement( index);
    String type = element.type();
    if ( type == null) type = "*";
    
    parentSchemaExpr.setVariable( "name", type);
    List<IModelObject> candidates = parentSchemaExpr.query( schema, null);
    if ( candidates.size() == 0) return schema.getParent().isType( "xs:schema");
    
    for ( IModelObject candidate: candidates)
      if ( compareBranch( candidate, path, index)) 
        return true;
    
    return false;
  }
  
  /**
   * Validate the type and restrictions (or extensions) of the value against the specified element 
   * schema and return the schema node which was violated if the value is invalid.  If the value
   * is not the correct type, then the attribute containing the primitive type is returned.  This
   * attribute may be "@base" or "@type".  If the value does not conform to an enumeration, then
   * the restriction containing the enumerations is returned.  If the value does not conform to
   * a numeric restriction, then the specified restriction is returned (e.g. minInclusive).  If
   * the value does not match the regular expression restriction, then the pattern node is 
   * returned.
   * @param schema The element schema.
   * @param value The value.
   * @return Returns null or the schema node which was violated.
   */
  static public IModelObject validate( IModelObject schema, String value)
  {
    IModelObject typeNode = getSimpleType( schema);
    String type = Xlate.get( typeNode, "");
    if ( !validateType( type, value)) return typeNode;
    
    if ( type.equals( "xs:string") || type.equals( "xs:token") || type.equals( "xs:normalizedString"))
    {
      IModelObject constraint = validateEnumeration( schema, value);
      if ( constraint != null) return constraint;
      
      constraint = validateStringLength( schema, value);
      if ( constraint != null) return constraint;
      
      constraint = validateRegex( schema, value);
      if ( constraint != null) return constraint;
      
      return null;
    }
    
    if ( type.equals( "xs:int") || 
        type.equals( "xs:long") ||
        type.equals( "xs:short") ||
        type.contains( "nteger") || 
        type.contains( "decimal") || 
        type.startsWith( "unsigned"))
    {
      return validateNumericRange( schema, value);
    }
    
    return null;
  }
  
  /**
   * Validate the type of the value from the schema.
   * @param type The type string from the schema (see getSimpleType()).
   * @param value The value.
   * @return Returns true if valid.
   */
  static public boolean validateType( String type, String value)
  {
    if ( type.startsWith( "xs:int"))
    {
      try { Integer.parseInt( value);} catch( Exception e) { return false;}
      return true;
    }
    else if ( type.equals( "xs:positiveInteger"))
    {
      try 
      { 
        int i = Integer.parseInt( value);
        return (i > 0);
      } 
      catch( Exception e) 
      { 
        return false;
      }
    }
    else if ( type.equals( "xs:nonPositiveInteger"))
    {
      try 
      { 
        int i = Integer.parseInt( value);
        return (i <= 0);
      } 
      catch( Exception e) 
      { 
        return false;
      }
    }
    else if ( type.equals( "xs:negativeInteger"))
    {
      try 
      { 
        int i = Integer.parseInt( value);
        return (i < 0);
      } 
      catch( Exception e) 
      { 
        return false;
      }
    }
    else if ( type.equals( "xs:nonNegativeInteger"))
    {
      try 
      { 
        int i = Integer.parseInt( value);
        return (i >= 0);
      } 
      catch( Exception e) 
      { 
        return false;
      }
    }
    else if ( type.equals( "xs:decimal"))
    {
      try { Double.parseDouble( value);} catch( Exception e) { return false;}
      return true;
    }
    else if ( type.equals( "xs:byte"))
    {
      try { Byte.parseByte( value);} catch( Exception e) { return false;}
      return true;
    }
    else if ( type.equals( "xs:long"))
    {
      try { Long.parseLong( value);} catch( Exception e) { return false;}
      return true;
    }
    else if ( type.equals( "xs:short"))
    {
      try { Short.parseShort( value);} catch( Exception e) { return false;}
      return true;
    }
    else if ( type.equals( "xs:unsignedLong"))
    {
      try 
      { 
        long l = Long.parseLong( value);
        return (l >= 0);
      } 
      catch( Exception e) 
      { 
        return false;
      }
    }
    else if ( type.equals( "xs:unsignedShort"))
    {
      try 
      { 
        short s = Short.parseShort( value);
        return (s >= 0);
      } 
      catch( Exception e) 
      { 
        return false;
      }
    }
    else if ( type.equals( "xs:unsignedInt"))
    {
      try 
      { 
        int i = Integer.parseInt( value);
        return (i >= 0);
      } 
      catch( Exception e) 
      { 
        return false;
      }
    }
    else if ( type.equals( "xs:unsignedByte"))
    {
      try 
      { 
        byte b = Byte.parseByte( value);
        return (b >= 0);
      } 
      catch( Exception e) 
      { 
        return false;
      }
    }
    else if ( type.equals( "xs:boolean"))
    {
      if ( !value.equals( "true") && !value.equals( "false")) return false;
    }
    
    // all string types
    return true;
  }
  
  /**
   * Validate the value with the enumeration restriction of the specified element schema.  If the
   * value is not one of the enumerations then the enumeration restriction is returned so that all
   * of the valid enumerations are known.
   * Type checking should already have been performed.
   * @param schema The element schema.
   * @param value The value.
   * @return Returns null or the violated enumeration restriction.
   */
  static public IModelObject validateEnumeration( IModelObject schema, String value)
  {
    IModelObject restriction = getEnumerations( schema);
    if ( restriction == null) return null;
    
    List<IModelObject> enumerations = restriction.getChildren( "xs:enumeration");
    if ( enumerations.size() == 0) return null;
    
    for( IModelObject enumeration: enumerations)
    {
      String option = Xlate.get( enumeration, "value", "");
      if ( value.equals( option)) return null;
    }
    
    return restriction;
  }
  
  /**
   * Validate the value against the numeric restrictions of the specified element schema.  If the value
   * is out of range, then the range element which was violated is returned.  If the value has too many
   * digits, then the "totalDigits" element is returned.
   * Type checking should already have been performed.
   * @param schema The element schema.
   * @param value The value.
   * @return Returns null or the violated numeric restriction.
   */
  static public IModelObject validateNumericRange( IModelObject schema, String value)
  {
    try
    {
      double number = Double.parseDouble( value);
      IModelObject restriction = restrictionExpr.queryFirst( schema);
      if ( restriction == null) return null;
      
      IModelObject minInclusive = restriction.getFirstChild( "xs:minInclusive");
      if ( minInclusive != null && number < Xlate.get( minInclusive, "value", 0)) return minInclusive;
      
      IModelObject minExclusive = restriction.getFirstChild( "xs:minExclusive");
      if ( minExclusive != null && number <= Xlate.get( minExclusive, "value", 0)) return minExclusive;
      
      IModelObject maxInclusive = restriction.getFirstChild( "xs:maxInclusive");
      if ( maxInclusive != null && number > Xlate.get( maxInclusive, "value", 0)) return maxInclusive;
      
      IModelObject maxExclusive = restriction.getFirstChild( "xs:maxExclusive");
      if ( maxExclusive != null && number >= Xlate.get( maxExclusive, "value", 0)) return maxExclusive;

      IModelObject totalDigits = restriction.getFirstChild( "xs:totalDigits");
      if ( totalDigits != null && value.trim().length() > Xlate.get( totalDigits, "value", 0)) return totalDigits;
      
      return null;
    }
    catch( NumberFormatException e)
    {
      e.printStackTrace( System.err);
      return null;
    }
  }

  /**
   * Validate the string length defined on the specified element schema.  If the value does not
   * match the string length constraints then the constraint element is returned.
   * @param schema The element schema.
   * @param value The value.
   * @return Returns null or the violated constraint.
   */
  static public IModelObject validateStringLength( IModelObject schema, String value)
  {
    IModelObject restriction = restrictionExpr.queryFirst( schema);
    if ( restriction == null) return null;

    int actualLength = value.length();
    IModelObject length = restriction.getFirstChild( "xs:length");
    if ( length != null && actualLength != Xlate.get( length, "value", 0)) return length;
    
    IModelObject minLength = restriction.getFirstChild( "xs:minLength");
    if ( minLength != null && actualLength < Xlate.get( minLength, "value", 0)) return minLength;

    IModelObject maxLength = restriction.getFirstChild( "xs:maxLength");
    if ( maxLength != null && actualLength > Xlate.get( maxLength, "value", 0)) return maxLength;
    
    return null;
  }

  /** 
   * Validate the value against the regular expression pattern of the specified element schema.  If the
   * value does not match the regular expression then the restriction pattern element is returned.
   * @param schema The element schema.
   * @param value The value.
   * @return Returns null or the violated pattern restriction.
   */
  static public IModelObject validateRegex( IModelObject schema, String value)
  {
    Pattern compiled = (Pattern)schema.getAttribute( "xm:pattern");
    if ( compiled == null)
    {
      try
      {
        IModelObject restriction = patternRestrictionExpr.queryFirst( schema);
        if ( restriction == null) return null;
        IModelObject pattern = restriction.getFirstChild( "xs:pattern");
        compiled = Pattern.compile( Xlate.get( pattern, "value", ""));
        schema.setAttribute( "xm:pattern", compiled);
      }
      catch( PatternSyntaxException e)
      {
        e.printStackTrace( System.err);
        return null;
      }
    }
    
    Matcher matcher = compiled.matcher( value);
    if ( !matcher.find()) 
    {
      IModelObject restriction = patternRestrictionExpr.queryFirst( schema);
      return restriction.getFirstChild( "xs:pattern");
    }
    
    return null;
  }
  
  final static IExpression elementTypeStringExpr = XPath.createExpression(
    "@type | */*/*/@base | */*/@base");
  
  final static IExpression namespacePrefixExpr = XPath.createExpression(
    "replace( name( ancestor-or-self::xs:schema/@*[ name() != 'targetNamespace' and . = $url]), '(^[^:=]+):?', '')");

  final static IExpression elementSchemaFinder = XPath.createExpression(
    "ancestor-or-self::xs:schema//xs:element[ @name = $name] | "+
    "ancestor-or-self::xs:schema//xs:attribute[ @name = $name]");

  final static IExpression rootElementSchemaFinder = XPath.createExpression(
    "ancestor-or-self::xs:schema/xs:element[ @name = $name]");
  
  final static IExpression parentSchemaExpr = XPath.createExpression(
    "ancestor::xs:element[ @name = $name]/self::*[ 1] | "+
    "(for $c in ancestor::xs:complexType[ 1]/@name return "+
    "  ancestor-or-self::xs:schema//xs:element[ @name = $name]/self::*[ @type = $c or */*/*[ @base = $c]])");

  final static IExpression typeDeclarationExpr = XPath.createExpression(
    "for $n in (@type | */*/*/@base | */*/@base) "+
    "return ancestor-or-self::xs:schema/*[ matches( name(), 'xs:simpleType|xs:complexType') and @name = $n]");
  
  final static IExpression simpleTypeExpr = XPath.createExpression(
    "(for $s in (@type | */xs:simpleContent/*/@base | xs:simpleType/*/@base) "+
    "return "+
    "  for $t in ancestor-or-self::xs:schema/*[ matches( name(), 'xs:simpleType|xs:complexType') and @name = $s] "+
    "  return ($t/@type | $t//@base))"+
    "| @type | */xs:simpleContent/*/@base | xs:simpleType/*/@base");
  
  final static IExpression enumRestrictionExpr = XPath.createExpression(
    "for $s in . "+
    "return "+
    "  if ( boolean( $s/xs:simpleType/*[ xs:enumeration])) then "+
    "    $s/xs:simpleType/*[ xs:enumeration] " +
    "  else "+
    "    ancestor-or-self::xs:schema/xs:simpleType[ @name = ($s/@type | $s/*/*/*/@base)]/*[ xs:enumeration]");

  final static IExpression patternRestrictionExpr = XPath.createExpression(
    "for $s in . "+
    "  return "+
    "    if ( boolean( $s/xs:simpleType/*[ xs:pattern])) then "+
    "      $s/xs:simpleType/*[ xs:pattern] " +
    "    else "+
    "      ancestor-or-self::xs:schema/xs:simpleType[ @name = ($s/@type | $s/*/*/*/@base)]/*[ xs:pattern]");
  
  final static IExpression restrictionExpr = XPath.createExpression(
    "for $s in . "+
    "return "+
    "  if ( boolean( $s/xs:simpleType/xs:restriction)) then "+
    "    $s/xs:simpleType/xs:restriction " +
    "  else "+
    "    ancestor-or-self::xs:schema/xs:simpleType[ @name = ($s/@type | $s/*/*/*/@base)]/xs:restriction");

  final static IExpression requiredAttributeExpr = XPath.createExpression(
    "descendant::xs:attribute[ @use = 'required'] |" +
    "(for $n in (@type | */*/*/@base | */*/@base) return " +
    "  ancestor-or-self::xs:schema/*[ matches( name(), 'xs:simpleType|xs:complexType') and @name = $n]//xs:attribute[ @use = 'required'])");
  
  private IModelObject root;
  private Set<String> includes;
  private Set<String> imports;
  
  public static void main( String[] args) throws Exception
  {
    Xsd xsd = new Xsd( new URL( "http://schema.stonewallnetworks.com/ns/entity/policy.xsd"));
    IPath path = XPath.createPath( "en:site/en:securityPolicy/en:ruleSet/en:securityRule/en:name");
    IModelObject schema = xsd.getElementSchema( path);
    System.out.println( "->"+schema);
  }
}
