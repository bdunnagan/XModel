/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.xpath.expression.ExpressionException;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * An X-Path function which reverses the order of the nodes in a node-set.
 */
public class ReverseFunction extends Function
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "reverse";
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NODES;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#evaluateNodes(
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);
    assertType( context, 0, ResultType.NODES);
    
    IExpression arg0 = getArgument( 0);
    List<IModelObject> nodes = arg0.evaluateNodes( context);
    Collections.reverse( nodes);
    
    return nodes;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyAdd(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, 
   * dunnagan.bob.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    IExpression parent = getParent();
    if ( parent != null) 
    {
      // obviously, this only reverses the order of the nodes which were added, but
      // this allows the listener to find the position of the first node in the node-set
      // and calculate the other positions from it.
      nodes = new ArrayList<IModelObject>( nodes);
      Collections.reverse( nodes);
      parent.notifyAdd( this, context, nodes);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyRemove(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, 
   * dunnagan.bob.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    IExpression parent = getParent();
    if ( parent != null) 
    {
      // obviously, this only reverses the order of the nodes which were removed, but
      // this allows the listener to find the position of the first node in the node-set
      // and calculate the other positions from it.
      nodes = new ArrayList<IModelObject>( nodes);
      Collections.reverse( nodes);
      parent.notifyRemove( this, context, nodes);
    }
  }
}
