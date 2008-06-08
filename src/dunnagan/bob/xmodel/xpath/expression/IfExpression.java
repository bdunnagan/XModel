/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.expression;

import java.util.List;

import dunnagan.bob.xmodel.IChangeSet;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IModelObjectFactory;

/**
 * An partial implementation of the X-Path 2.0 for/return expression. Since this is a hybrid
 * X-Path 1.0/2.0 implementation, both the iterator and the return expressions must return
 * node-sets.
 */
public class IfExpression extends Expression
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "if-then-else";
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return getArgument( 1).getType();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#getType(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public ResultType getType( IContext context)
  {
    return getArgument( 1).getType( context);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#evaluateNodes(
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    assertType( context, 2, getArgument( 1).getType( context));
 
    IExpression condition = getArgument( 0);
    IExpression thenExpression = getArgument( 1);
    IExpression elseExpression = getArgument( 2);
    
    if ( condition.evaluateBoolean( context))
      return thenExpression.evaluateNodes( context);
    else
      return elseExpression.evaluateNodes( context);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#evaluateBoolean(
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    assertType( context, 2, getArgument( 1).getType( context));
    
    IExpression condition = getArgument( 0);
    IExpression thenExpression = getArgument( 1);
    IExpression elseExpression = getArgument( 2);
    
    if ( condition.evaluateBoolean( context))
      return thenExpression.evaluateBoolean( context);
    else
      return elseExpression.evaluateBoolean( context);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#evaluateNumber(
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    assertType( context, 2, getArgument( 1).getType( context));
    
    IExpression condition = getArgument( 0);
    IExpression thenExpression = getArgument( 1);
    IExpression elseExpression = getArgument( 2);
    
    if ( condition.evaluateBoolean( context))
      return thenExpression.evaluateNumber( context);
    else
      return elseExpression.evaluateNumber( context);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#evaluateString(
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public String evaluateString( IContext context) throws ExpressionException
  {
    assertType( context, 2, getArgument( 1).getType( context));
    
    IExpression condition = getArgument( 0);
    IExpression thenExpression = getArgument( 1);
    IExpression elseExpression = getArgument( 2);
    
    if ( condition.evaluateBoolean( context))
      return thenExpression.evaluateString( context);
    else
      return elseExpression.evaluateString( context);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#createSubtree(dunnagan.bob.xmodel.xpath.expression.IContext, 
   * dunnagan.bob.xmodel.IModelObjectFactory, dunnagan.bob.xmodel.IChangeSet)
   */
  @Override
  public void createSubtree( IContext context, IModelObjectFactory factory, IChangeSet undo)
  {
    if ( getArgument( 0).evaluateBoolean( context))
    {
      getArgument( 1).createSubtree( context, factory, undo);
    }
    else
    {
      getArgument( 2).createSubtree( context, factory, undo);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#bind(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public void bind( IContext context)
  {
    IExpression condition = getArgument( 0);
    condition.bind( context);
    try
    {
      if ( condition.evaluateBoolean( context))
      {
        getArgument( 1).bind( context);
      }
      else
      {
        getArgument( 2).bind( context);
      }
    }
    catch( ExpressionException e)
    {
      if ( parent != null) parent.handleException( this, context, e);
    }
  }

  /**
   * Reevaluate the condition expression and rebind the argument expressions as necessary.
   * @param context The context.
   */
  public void rebind( IContext context)
  {
    IExpression condition = getArgument( 0);
    
    context.getModel().revert();
    IExpression arg1 = getArgument( 1);
    IExpression arg2 = getArgument( 2);
    if ( condition.evaluateBoolean( context))
      arg1.unbind( context);
    else
      arg2.unbind( context);

    context.getModel().restore();
    try
    {
      if ( condition.evaluateBoolean( context))
      {
        arg1.bind( context);
      }
      else
      {
        arg2.bind( context);
      }
    }
    catch( ExpressionException e)
    {
      if ( parent != null) parent.handleException( this, context, e);
    }
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyAdd(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    IExpression condition = getArgument( 0);
    if ( expression == condition)
    {
      rebind( context);
      //FIXME: this could be more efficient
      notifyChange( this, context);
    }
    else
    {
      parent.notifyAdd( this, context, nodes);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyRemove(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    IExpression condition = getArgument( 0);
    if ( expression == condition)
    {
      rebind( context);
      //FIXME: this could be more efficient
      notifyChange( this, context);
    }
    else
    {
      parent.notifyRemove( this, context, nodes);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
   * boolean)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    IExpression condition = getArgument( 0);
    if ( expression == condition)
    {
      rebind( context);
      //FIXME: this could be more efficient
      notifyChange( this, context);
    }
    else
    {
      parent.notifyChange( this, context, newValue);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
   * double, double)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    IExpression condition = getArgument( 0);
    if ( expression == condition)
    {
      rebind( context);
      //FIXME: this could be more efficient
      notifyChange( this, context);
    }
    else
    {
      parent.notifyChange( this, context, newValue, oldValue);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
   * java.lang.String, java.lang.String)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    IExpression condition = getArgument( 0);
    if ( expression == condition)
    {
      rebind( context);
      //FIXME: this could be more efficient
      notifyChange( this, context);
    }
    else
    {
      parent.notifyChange( this, context, newValue, oldValue);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context)
  {
    IExpression condition = getArgument( 0);
    if ( expression == condition) rebind( context);
    parent.notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#notifyValue(java.util.List, 
   * dunnagan.bob.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    IExpression condition = getArgument( 0);
    if ( expression == condition) rebind( contexts[ 0]);
    parent.notifyValue( this, contexts, object, newValue, oldValue);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#requiresValueNotification(
   * dunnagan.bob.xmodel.xpath.expression.IExpression)
   */
  @Override
  public boolean requiresValueNotification( IExpression argument)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append( "if (");
    builder.append( getArgument( 0).toString());
    builder.append( ") then (");
    builder.append( getArgument( 1).toString());
    builder.append( ") else (");
    builder.append( getArgument( 2).toString());
    builder.append( ')');
    return builder.toString();
  }
}
