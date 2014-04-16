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
 * An XPath function that finds the node from a node-set that maximizes an expression.
 */
public class MaximizeFunction extends Function
{
  public static String name = "maximize";
  
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
    IExpression maxExpr = getArgument( 1);
    
    List<IModelObject> nodes = getArgument( 0).evaluateNodes( context);
    if ( nodes.size() == 0) return Collections.emptyList();
    
    IModelObject maxNode = nodes.get( 0);
    double max = maxExpr.evaluateNumber( new Context( context, maxNode));
    for( int i=1; i<nodes.size(); i++)
    {
      double value = maxExpr.evaluateNumber( new Context( context, nodes.get( i)));
      if ( max < value)
      {
        maxNode = nodes.get( i);
        max = value; 
      }
    }
    
    return (maxNode != null)? Collections.singletonList( maxNode): Collections.<IModelObject>emptyList();
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
