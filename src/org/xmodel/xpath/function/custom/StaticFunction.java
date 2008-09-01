/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xpath.function.custom;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.function.Function;


/**
 * This function is not part of the XPath 1.0 specification.  It takes a single argument expression
 * which may have any return type and returns the result of the expression.  However, when this
 * function is bound, it does not bind its argument which is useful for optimizing expressions
 * which will be bound and which have subtrees which will never change.
 */
public class StaticFunction extends Function
{

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "static";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return getArgument( 0).getType();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#getType(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public ResultType getType( IContext context)
  {
    return getArgument( 0).getType( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateBoolean(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);
    return getArgument( 0).evaluateBoolean( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNodes(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);
    return getArgument( 0).evaluateNodes( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNumber(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);
    return getArgument( 0).evaluateNumber( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateString(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public String evaluateString( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);
    return getArgument( 0).evaluateString( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#bind(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void bind( IContext context)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#unbind(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void unbind( IContext context)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#isAbsolute()
   */
  public boolean isAbsolute( IContext context)
  {
    return true;
  }
}
