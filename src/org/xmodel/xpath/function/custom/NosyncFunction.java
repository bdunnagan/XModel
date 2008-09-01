/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xpath.function.custom;

import java.util.List;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.function.Function;


/**
 * A custom XPath function which takes a single argument and returns the result of the argument.
 * This function is a noop except that it guarantees that no external references addressed by
 * the argument query will be synchronized during the query.
 */
public class NosyncFunction extends Function
{
  public final static String name = "nosync";
  
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
    IModel model = context.getModel();
    try
    {
      model.setSyncLock( true);
      return getArgument( 0).evaluateBoolean( context);
    }
    finally
    {
      model.setSyncLock( false);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNodes(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);
    IModel model = context.getModel();
    try
    {
      model.setSyncLock( true);
      return getArgument( 0).evaluateNodes( context);
    }
    finally
    {
      model.setSyncLock( false);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNumber(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);
    IModel model = context.getModel();
    try
    {
      model.setSyncLock( true);
      return getArgument( 0).evaluateNumber( context);
    }
    finally
    {
      model.setSyncLock( false);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateString(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public String evaluateString( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);
    IModel model = context.getModel();
    try
    {
      model.setSyncLock( true);
      return getArgument( 0).evaluateString( context);
    }
    finally
    {
      model.setSyncLock( false);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#bind(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void bind( IContext context)
  {
    IModel model = context.getModel();
    try
    {
      model.setSyncLock( true);
      super.bind( context);
    }
    finally
    {
      model.setSyncLock( false);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#unbind(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void unbind( IContext context)
  {
    IModel model = context.getModel();
    try
    {
      model.setSyncLock( true);
      super.unbind( context);
    }
    finally
    {
      model.setSyncLock( false);
    }
  }
  
}
