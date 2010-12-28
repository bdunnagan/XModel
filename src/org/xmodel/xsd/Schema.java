/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * Schema.java
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
package org.xmodel.xsd;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xmodel.*;
import org.xmodel.external.ExternalReference;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.UnboundedCache;
import org.xmodel.external.caching.FileCachingPolicy;
import org.xmodel.xpath.AttributeNode;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;
import org.xmodel.xsd.check.*;
import org.xmodel.xsd.check.SchemaError.Type;


/**
 * A class containing algorithms for examining a simplified schema.
 */
public class Schema
{
  /**
   * Finds the schema element definition which most closely matches the specified element.
   * @param schema The schema root.
   * @param element The element.
   * @return Returns the matching schema element definition.
   */
  public static IModelObject findSchema( IModelObject schema, IModelObject element)
  {
    int matchLength = 0;
    IModelObject matchLeaf = null;
    
    List<IModelObject> leaves = null;
    if ( element instanceof AttributeNode)
    {
      findMatchingAttributeLeavesExpr.setVariable( "name", element.getType());
      leaves = findMatchingAttributeLeavesExpr.query( schema, null);
    }
    else
    {
      findMatchingElementLeavesExpr.setVariable( "name", element.getType());
      leaves = findMatchingElementLeavesExpr.query( schema, null);
    }
    
    for( IModelObject leaf: leaves)
    {
      int length = findMatchLength( leaf, element);
      if ( length > matchLength)
      {
        matchLength = length;
        matchLeaf = leaf;
      }
    }
    
    return matchLeaf;
  }
  
  /**
   * Returns the number of ancestors of the specified schema leaf which match the specified element.
   * @param leaf A schema leaf which matches the type of the element argument.
   * @param element The element.
   * @return Returns the matching length of ancestors.
   */
  private static int findMatchLength( IModelObject leaf, IModelObject element)
  {
    int count = 0;
    while( leaf != null && element != null)
    {
      String leafType = Xlate.get( leaf, "name", "");
      if ( !leafType.equals( element.getType())) break;
      leaf = leaf.getParent();
      if ( leaf == null) break;
      count++;
    }
    return count;
  }

  /**
   * Validate the specified document against the specified schema.
   * @param schema The simplified schema or schema fragment.
   * @param document The document to be validated.
   * @return Returns true if the document is valid.
   */
  public static boolean validate( IModelObject schema, IModelObject document)
  {
    String type = schema.getType();
    ICheck check = null;
    if ( type.equals( "schema")) check = new SchemaCheck( schema);
    else if ( type.equals( "element")) check = new ElementCheck( schema);
    else if ( type.equals( "attribute") || type.equals( "value")) check = new ValueCheck( schema);
    return check.validate( document);
  }
  
  /**
   * Validate the specified document against the specified schema and optionally annotate the document.
   * @param schema The simplified schema or schema fragment.
   * @param document The document to be validated.
   * @param annotate True if the document should be annotated with the validation errors.
   * @return Returns the list of schema errors.
   */
  public static List<SchemaError> validate( IModelObject schema, IModelObject document, boolean annotate)
  {
    String type = schema.getType();
    ICheck check = null;
    if ( type.equals( "schema")) check = new SchemaCheck( schema);
    else if ( type.equals( "element")) check = new ElementCheck( schema);
    else if ( type.equals( "attribute") || type.equals( "value")) check = new ValueCheck( schema);
    
    removeAnnotations( document);
    if ( !check.validate( document))
    {
      List<SchemaError> errors = new ArrayList<SchemaError>();
      check.getErrors( errors);
      if ( annotate && false) for( SchemaError error: errors) error.annotate();
      return errors;
    }
    
    return Collections.emptyList();
  }
  
  /**
   * Remove all annotation elements from the specified document.
   * @param document The document.
   */
  public static void removeAnnotations( IModelObject document)
  {
    for( IModelObject annotation: annotationPath.query( document, null))
      annotation.removeFromParent();
  }
  
  /**
   * Create a default document for the leaf of the specified trace and include ancestors to the root of the 
   * trace. This method is useful for creating a document and simultaneously localizing it via its ancestry.
   * @param trace The schema trace.
   * @param optional True if optional nodes should be created.
   * @return Returns the new document.
   */
  public static IModelObject createDocumentBranch( SchemaTrace trace, boolean optional)
  {
    // create complete document for leaf
    IModelObject leaf = createDocument( trace.getLeaf(), optional);
    IModelObject node = leaf;
    
    // create all ancestors in trace except the root of the schema (i == 0)
    for( int i = trace.getLength() - 2; i > 0; i--)
    {
      IModelObject schema = trace.getElement( i);
      String name = Xlate.get( schema, "name", "");
      IModelObject parent = new ModelObject( name);
      parent.addChild( node);
      node = parent;
    }
    
    return leaf;
  }
  
  /**
   * Create a default document from the specified element schema.
   * @param schema The simplified schema for the element.
   * @param optional True if optional nodes should be created.
   * @return Returns the new document.
   */
  public static IModelObject createDocument( IModelObject schema, boolean optional)
  {
    return createDocument( schema, null, optional);
  }

  /**
   * Create a default document from the specified element schema.
   * @param schema The simplified schema for the element.
   * @param factory The factory for creating new objects.
   * @param optional True if optional nodes should be created.
   * @return Returns the new document.
   */
  public static IModelObject createDocument( IModelObject schema, IModelObjectFactory factory, boolean optional)
  {
    return createDocument( schema, factory, optional, new ArrayList<IModelObject>());
  }
  
  /**
   * Create a default document from the specified element schema.
   * @param schema The simplified schema for the element.
   * @param factory The factory for creating new objects.
   * @param optional True if optional nodes should be created.
   * @param traversed The current stack of element schemas.
   * @return Returns the new document.
   */
  public static IModelObject createDocument( IModelObject schema, IModelObjectFactory factory, boolean optional, List<IModelObject> traversed)
  {
    if ( !schema.isType( "element"))
      throw new IllegalArgumentException( "Argument must be a simplified schema element tag: "+schema);

    // push current element schema on recursion stack
    traversed.add( schema);
    
    try
    {
      // create element
      String type = Xlate.get( schema, "name", (String)null);
      if ( type == null) type = Xlate.get( schema, "type", (String)null);
      IModelObject document = (factory != null)? factory.createObject( null, type): new ModelObject( type);
      
      // create attributes
      List<IModelObject> attributes = schemaAttributesPath.query( schema, null);
      for( IModelObject attribute: attributes)
      {
        boolean required = Xlate.get( attribute, "use", "optional").equals( "required");
        if ( required || optional)
        {
          String attrName = Xlate.get( attribute, "name", "");
          String attrDefault = Xlate.get( attribute, "default", "");
          document.setAttribute( attrName, attrDefault);
        }
      }
      
      // create value
      IModelObject value = schema.getFirstChild( "value");
      if ( value != null)
      {
        String valueDefault = Xlate.get( value, "default", "");
        document.setValue( valueDefault);
      }
      
      // create children, but halt recursion
      IModelObject constraint = schemaConstraintPath.queryFirst( schema);
      if ( constraint != null)
      {
        List<IModelObject> children = new ArrayList<IModelObject>();
        createChildren( constraint, factory, children, optional, traversed);
        for( IModelObject child: children) document.addChild( child);
      }
      
      return document;
    }
    finally
    {
      // pop current schema off recursion stack
      traversed.remove( schema);
    }
  }
  
  /**
   * Create the children specified by the given simplified schema constraint.
   * @param schema A set, list, choice or child constraint.
   * @param factory The factory for creating objects.
   * @param children The list where the children will be appended.
   * @param optional True if optional nodes should be created.
   * @param traversed The current stack of element schemas.
   */
  private static void createChildren( IModelObject schema, IModelObjectFactory factory, List<IModelObject> children, boolean optional, List<IModelObject> traversed)
  {
    // check min occurrence
    int min = Xlate.get( schema, "min", 1);
    if ( min == 0 && !optional) return;
    
    // create children
    boolean allChoices = true;
    if ( schema.isType( "set") || schema.isType( "list") || (schema.isType( "choice") && allChoices))
    {
      List<IModelObject> constraints = schema.getChildren();
      for( IModelObject constraint: constraints) createChildren( constraint, factory, children, optional, traversed);
    }
    else if ( schema.isType( "choice"))
    {
      IModelObject constraint = schema.getChild( 0);
      createChildren( constraint, factory, children, optional, traversed);
    }
    else if ( schema.isType( "child"))
    {
      StatefulContext context = new StatefulContext( schema);
      context.set( "name", Xlate.get( schema, ""));
      schema = resolveChildExpr.queryFirst( context);
      if ( !traversed.contains( schema)) children.add( createDocument( schema, factory, optional));
    }
  }

  /**
   * Remove all elements from the document which are marked as invalid by the validation tree.
   * @param schema The simplified schema.
   * @param document The document.
   * @return Returns a clone of the document with the elements removed.
   */
  public static IModelObject removeInvalidElements( IModelObject schema, IModelObject document)
  {
    document = document.cloneTree();
    List<SchemaError> errors = validate( schema, document, false);
    if ( errors.size() == 0) return document;
    
    // cull
    for( SchemaError error: errors)
    {
      if ( error.isType( Type.illegalElement))
      {
        IModelObject locus = error.getDocumentLocus();
        locus.removeFromParent();
      }
    }
    return document;
  }
  
  private final static IPath schemaAttributesPath = XPath.createPath( 
    "attributes/attribute");
  
  private final static IPath schemaConstraintPath = XPath.createPath( 
    "constraint/*");
  
  private final static IExpression resolveChildExpr = XPath.createExpression(
    "ancestor::element/children/element[ @name = $name]");
  
  private final static IPath annotationPath = XPath.createPath(
    "descendant::schema:errors");
  
  private final static IExpression findMatchingElementLeavesExpr = XPath.createExpression(
    ".//element[ @name = $name]");
  
  private final static IExpression findMatchingAttributeLeavesExpr = XPath.createExpression(
    ".//attribute[ @name = $name]");
  
  public static void main( String[] args) throws Exception
  {
    if ( args.length < 2)
    {
      System.out.println( "usage: java Schema <xsd> <document>\n");
      return;
    }
    
    File xsdFile = new File( args[ 0]);
    IExternalReference xsd = new ExternalReference( "xsd");
    xsd.setAttribute( "url", xsdFile.toURL());
    xsd.setCachingPolicy( new XsdCachingPolicy( new UnboundedCache()));
    xsd.setDirty( true);

    File docFile = new File( args[ 1]);
    IExternalReference doc = new ExternalReference( "doc");
    doc.setAttribute( "path", docFile.toString());
    doc.setCachingPolicy( new FileCachingPolicy( new UnboundedCache()));
    doc.setDirty( true);
    
    SchemaTransform transform = new SchemaTransform();
    IModelObject transformed = transform.transform( xsd.getChild( 0));
    
    // validate generated schema against schema.xsd
    List<SchemaError> errors = Schema.validate( transformed, doc.getChild( 0), true);
    for( SchemaError error: errors)
      System.out.println( error);

    // look for leftovers
    errors = Schema.validate( transformed, doc.getChild( 0), true);
    for( SchemaError error: errors)
      System.out.println( error);
  }
}
