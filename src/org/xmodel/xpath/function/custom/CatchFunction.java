package org.xmodel.xpath.function.custom;

import java.util.Collections;
import java.util.List;

import org.xmodel.INode;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.function.Function;

/**
 * A custom XPath function that catches instances of ExpressionException. The function takes two or
 * three arguments.  The function returns the result of the first argument.  If an ExpressionException
 * is caught, then the function sets the first node returned by the second argument with the
 * message in the exception.  When an exception is caught, the function will return a default value.
 * If the third argument is provided, then it must evaluate to the same type as the first argument,
 * and its value is used as the default value.
 * According to the XPath 2.0 documentation:
 * <quote>The method by which an XPath processor reports error information to the external environment 
 * is implementation-defined.</quote>
 */
public class CatchFunction extends Function 
{
  public final static String name = "catch";
  
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
   * @see org.xmodel.xpath.expression.Expression#evaluateNodes(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public List<INode> evaluateNodes( IContext context) throws ExpressionException 
  {
    try
    {
      return getArgument( 0).evaluateNodes(context);
    }
    catch( ExpressionException e)
    {
      INode node = getArgument( 1).queryFirst( context);
      if ( node != null) Xlate.set( node, e.getMessage());
      
      if ( getArguments().size() == 3)
      {
        return getArgument( 2).evaluateNodes( context);
      }
      
      return Collections.emptyList();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNumber(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public double evaluateNumber( IContext context) throws ExpressionException 
  {
    try
    {
      return getArgument( 0).evaluateNumber(context);
    }
    catch( ExpressionException e)
    {
      INode node = getArgument( 1).queryFirst( context);
      if ( node != null) Xlate.set( node, e.getMessage());
      
      if ( getArguments().size() == 3)
      {
        return getArgument( 2).evaluateNumber( context);
      }

      return 0;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateString(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public String evaluateString( IContext context) throws ExpressionException 
  {
    try
    {
      return getArgument( 0).evaluateString(context);
    }
    catch( ExpressionException e)
    {
      INode node = getArgument( 1).queryFirst( context);
      if ( node != null) Xlate.set( node, e.getMessage());
      
      if ( getArguments().size() == 3)
      {
        return getArgument( 2).evaluateString( context);
      }

      return "";
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateBoolean(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public boolean evaluateBoolean( IContext context) throws ExpressionException 
  {
    try
    {
      return getArgument( 0).evaluateBoolean(context);
    }
    catch( ExpressionException e)
    {
      INode node = getArgument( 1).queryFirst( context);
      if ( node != null) Xlate.set( node, e.getMessage());
      
      if ( getArguments().size() == 3)
      {
        return getArgument( 2).evaluateBoolean( context);
      }

      return false;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#bind(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void bind( IContext context) 
  {
    getArgument( 0).bind(context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#unbind(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void unbind( IContext context) 
  {
    getArgument( 0).unbind(context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd(IExpression expression, IContext context, List<INode> nodes) 
  {
    if ( expression == getArgument( 0))
    {
      getParent().notifyAdd( this, context, nodes);
    }
    else
    {
      getParent().notifyChange( this, context);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove(IExpression expression, IContext context, List<INode> nodes) 
  {
    if ( expression == getArgument( 0))
    {
      getParent().notifyRemove( this, context, nodes);
    }
    else
    {
      getParent().notifyChange( this, context);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, boolean)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, boolean newValue) 
  {
    if ( expression == getArgument( 0))
    {
      getParent().notifyChange( this, context, newValue);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, double, double)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue) 
  {
    if ( expression == getArgument( 0))
    {
      getParent().notifyChange( this, context, newValue, oldValue);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, java.lang.String, java.lang.String)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue) 
  {
    if ( expression == getArgument( 0))
    {
      getParent().notifyChange( this, context, newValue, oldValue);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context) 
  {
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyValue(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext[], org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, INode object, Object newValue, Object oldValue) 
  {
    if ( expression == getArgument( 0))
    {
      getParent().notifyValue( this, contexts, object, newValue, oldValue);
    }
  }
}
