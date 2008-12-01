/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xsd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.Xlate;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.IExpression;


/**
 * A class which contains the list of schema elements for each ancestor of a particular element. The schema
 * trace begins with the schema root followed by the first ancestor which is a global element in the target 
 * schema. The last schema locus in the trace is either an element schema, an attribute schema or an element 
 * value schema.
 */
public class SchemaTrace
{
  protected SchemaTrace()
  {
  }
  
  /**
   * Returns the specified trace element.
   * @param i The index.
   * @return Returns the specified trace element.
   */
  public IModelObject getElement( int i)
  {
    return trace.get( i);
  }

  /**
   * Returns the leaf of the trace.
   * @return Returns the leaf of the trace.
   */
  public IModelObject getLeaf()
  {
    return trace.get( trace.size() - 1);
  }

  /**
   * Returns the length of the trace.
   * @return Returns the length of the trace.
   */
  public int getLength()
  {
    return trace.size();
  }
  
  /**
   * Returns true if the element schema or attribute schema has a minimum occurence of zero. When the leaf
   * of the trace is an element schema, the element is optional if any of its ancestors are optional.
   * @return Returns true if the element schema or attribute schema has a minimum occurence of zero.
   */
  public boolean isOptional()
  {
    // global elements are optional
    if ( trace.size() <= 2) return true;
    
    // get leaf of trace
    IModelObject leaf = trace.get( trace.size() - 1);
    
    // use element parent of value leaf
    if ( leaf.isType( "value")) leaf = leaf.getParent();;
    
    // check optional attribute
    if ( leaf.isType( "attribute")) 
      return Xlate.get( leaf, "use", "optional").equals( "optional");

    // check if leaf is optional
    if ( isOptional( leaf)) return true;
    
    // check ancestors
    for( int index = trace.size() - 2; index >= 0; index--)
    {
      IModelObject ancestor = trace.get( index);
      if ( isOptional( ancestor)) return true;
    }
    
    return false;
  }
  
  /**
   * Returns the list of all optional nodes in the schema trace. If the schema trace only
   * contains a global element, then the global element will be returned since global elements
   * are always optional.
   * @return Returns the list of all optional nodes in the schema trace.
   */
  public List<IModelObject> getOptionals()
  {
    // nothing in trace
    if ( trace.size() == 0) return Collections.emptyList();
    
    // global elements are optional
    if ( trace.size() <= 2) return Collections.singletonList( trace.get( 0));

    // get leaf of trace
    IModelObject leaf = trace.get( trace.size() - 1);
    
    // use element parent of value leaf
    if ( leaf.isType( "value")) leaf = leaf.getParent();;

    // create result list
    List<IModelObject> optionals = new ArrayList<IModelObject>();
    
    // check optional attribute
    if ( leaf.isType( "attribute") && Xlate.get( leaf, "use", "optional").equals( "optional"))
      optionals.add( leaf);

    // check if leaf is optional
    int index = trace.size() - 2;
    IModelObject parent = trace.get( index);
    childConstraintFinder.setVariable( "name", Xlate.get( leaf, "name", ""));
    IModelObject constraint = childConstraintFinder.queryFirst( parent);
    if ( Xlate.get( constraint, "min", 1) == 0) optionals.add( leaf);
    
    // check ancestors
    for( ; index >= 0; index--)
    {
      IModelObject ancestor = trace.get( index);
      childConstraintFinder.setVariable( "name", Xlate.get( trace.get( index+1), "name", ""));
      constraint = childConstraintFinder.queryFirst( ancestor);
      if ( Xlate.get( constraint, "min", 1) == 0) optionals.add( ancestor);
    }
    
    return optionals;
  }
  
  /**
   * Returns the schema elements which comprise the path from the specified schema root to the specified node.
   * If the node is an attribute node then the last item in the list will be a schema attribute entry. If the
   * node is a text node then the last item will be a schema value entry.
   * @param schemaRoot The root of the simplified schema.
   * @param node The non-global document node (may be an attribute or value node).
   * @return Returns the schema path of the specified node.
   */
  private List<IModelObject> createSchemaPath( IModelObject schemaRoot, IModelObject node)
  {
    List<IModelObject> result = new ArrayList<IModelObject>();
    result.add( schemaRoot);
    
    // make list of ancestors of node plus node itself
    List<IModelObject> ancestors = new ArrayList<IModelObject>();
    IModelObject ancestor = node;
    while( ancestor != null)
    {
      ancestors.add( 0, ancestor);
      ancestor = ancestor.getParent();
    }

    // search for global element from root to node
    int ancestorIndex = 0;
    IModelObject schema = null;
    for( ; ancestorIndex < ancestors.size(); ancestorIndex++)
    {
      IModelObject globalElement = ancestors.get( ancestorIndex);
      globalElementFinder.setVariable( "name", globalElement.getType());
      schema = globalElementFinder.queryFirst( schemaRoot);
      if ( schema != null) break;
    }

    // add global element
    if ( schema != null) result.add( schema);
    
    // search for schema fragment
    for( ancestorIndex++; ancestorIndex < ancestors.size() && schema != null; ancestorIndex++)
    {
      IModelObject globalElement = ancestors.get( ancestorIndex);
      String name = globalElement.getType();
      
      // test name indicates a value node
      if ( name.length() > 0)
      {
        // look for an child element schema
        childElementFinder.setVariable( "name", name);
        IModelObject childSchema = childElementFinder.queryFirst( schema);
        if ( childSchema == null)
        {
          // look for an attribute schema
          elementAttributeFinder.setVariable( "name", globalElement.getType());
          childSchema = elementAttributeFinder.queryFirst( schema);
          if ( childSchema == null) return null;
        }
        schema = childSchema;
      }
      else
      {
        // only the value node has an empty name
        schema = schema.getFirstChild( "value");
      }
      
      result.add( schema);
    }
   
    if ( result.size() == 1) return Collections.emptyList();
    return result;
  }

  /**
   * Returns true if the specified element schema denotes an optional element. An element is optional
   * if any of its ancestors are optional and do not have a required element.  A required attribute
   * will not change the optional status of an ancestor, however. 
   * @param elementSchema The element schema.
   * @return Returns true if the element schema is optional.
   */
  public boolean isOptional( IModelObject elementSchema)
  {
    return isOptionalExpr.evaluateBoolean( new Context( elementSchema));
  }
  
  /**
   * Create a SchemaTrace for the specified node in the specified target schema.
   * @param schema The root of the target schema.
   * @param node A node whose partial path can be found in the schema.
   * @return Returns null or the SchemaTrace for the node.
   */
  public static SchemaTrace getInstance( IModelObject schema, IModelObject node)
  {
    SchemaTrace trace = new SchemaTrace();
    trace.trace = trace.createSchemaPath( schema, node);
    return (trace.trace != null && trace.trace.size() > 0)? trace: null;
  }
  
  private IPath globalElementFinder = XPath.createPath(
    "element[ @name = $name]");

  private IPath childElementFinder = XPath.createPath(
    "children/element[ @name = $name]");

  private IPath elementAttributeFinder = XPath.createPath(
    "attributes/attribute[ @name = $name]");
  
  private IPath childConstraintFinder = XPath.createPath(
    "constraint//child[ . = $name]");
  
  private IExpression isOptionalExpr = XPath.createExpression(
    "let $name := @name;" +
    "let $constraint := ../../constraint//child[ . = $name];" +
    "let $element := ../../children/element[ @name = $name];" +
    "($constraint/@min = 0) and not( $element/constraint//child[ not( @min = 0)])");
  
  private List<IModelObject> trace;
}
