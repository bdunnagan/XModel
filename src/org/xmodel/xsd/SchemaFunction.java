/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xsd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.function.Function;


/**
 * A custom XPath function which returns an element or attribute definition from a simplified schema.
 * The first argument is the root of the simplified schema. The second argument is either an node-set
 * or a string. If the second argument is a node-set, then the schema is searched for a matching 
 * schema definition for each node (see SchemaTrace for details). If the second argument is a string,
 * then that path is returned from the schema. The path may start with either a global complex type
 * or a global element.
 */
public class SchemaFunction extends Function
{
  public final static String name = "schema";
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return name;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NODES;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNodes(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    assertArgs( 2, 2);
    assertType( context, 1, ResultType.NODES);
    
    IModelObject schemaRoot = getArgument( 0).queryFirst( context);
    if ( schemaRoot == null) return Collections.emptyList();
    
    List<IModelObject> result = new ArrayList<IModelObject>();
    
    ResultType type = getArgument( 1).getType( context);
    if ( type == ResultType.NODES)
    {
      for( IModelObject node: getArgument( 1).evaluateNodes( context))
      {
        IModelObject schema = Schema.findSchema( schemaRoot, node); 
        if ( schema != null) result.add( schema);
      }
    }
    else if ( type == ResultType.STRING) 
    {
      String path = getArgument( 1).evaluateString( context);
      String[] parts = path.split( "/");
      globalFinder.setVariable( "name", parts[ 0]);
      IModelObject schema = globalFinder.queryFirst( schemaRoot);
      for( int i=1; i<parts.length; i++)
      {
        IModelObject childSchema = findChild( schema, parts[ i]);
        if ( childSchema == null) break;
        schema = childSchema;
      }
      if ( schema != null) result.add( schema);
    }
    else
    {
      throw new ExpressionException( this, "Second argument must return node-set or string.");
    }
      
    return result;
  }
  
  /**
   * Find a child schema element with the specified name.
   * @param parent The parent schema element.
   * @param name The name of the child.
   * @return Returns the child schema element.
   */
  private IModelObject findChild( IModelObject parent, String name)
  {
    childFinder.setVariable( "name", name);
    return childFinder.queryFirst( parent);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.lang.String, java.lang.String)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    getParent().notifyChange( this, context);
  }
  
  private IExpression globalFinder = XPath.createExpression(
    "element[ @type = $name or @name = $name]");
  
  private IExpression childFinder = XPath.createExpression(
    "children/element[ @name = $name]");
}
