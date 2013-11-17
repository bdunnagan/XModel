package org.xmodel.xpath.function.custom;

import java.util.Collections;
import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.function.Function;

/**
 * An XPath function that finds the node from a node-set that minimizes an expression.
 */
public class MinimizeFunction extends Function
{
  public static String name = "minimize";
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  @Override
  public String getName()
  {
    return name;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  @Override
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
    IExpression minExpr = getArgument( 1);
    
    List<IModelObject> nodes = getArgument( 0).evaluateNodes( context);
    if ( nodes.size() == 0) return Collections.emptyList();
    
    IModelObject minNode = nodes.get( 0);
    double min = minExpr.evaluateNumber( new Context( context, minNode));
    for( int i=1; i<nodes.size(); i++)
    {
      double value = minExpr.evaluateNumber( new Context( context, nodes.get( i)));
      if ( min > value)
      {
        minNode = nodes.get( i);
        min = value; 
      }
    }
    
    return (minNode != null)? Collections.singletonList( minNode): Collections.<IModelObject>emptyList();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( getParent() != null) notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( getParent() != null) notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyValue(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext[], org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    if ( getParent() != null) notifyChange( this, contexts[ 0]);
  }
}
