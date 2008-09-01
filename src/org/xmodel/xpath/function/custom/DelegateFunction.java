/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xpath.function.custom;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.ExpressionListener;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpressionListener;
import org.xmodel.xpath.expression.LetExpression;
import org.xmodel.xpath.expression.RootExpression;
import org.xmodel.xpath.function.Function;
import org.xmodel.xpath.variable.IVariableSource;


/**
 * A custom xpath function which encapsulates an xpath expression.  This makes it simple 
 * to create custom xpath functions for common queries.  The delegate function passes its
 * arguments to the delegate expression by assigning variables on the expression.  The
 * variables are $arg0, $arg1, etc....  The arguments are not type-checked or counted.
 */
public class DelegateFunction extends Function
{
  /**
   * Create a function with the specified name that delegates to the specified expression.
   * @param name The name of the function.
   * @param spec The xpath specification.
   */
  protected DelegateFunction( String name, String spec)
  {
    this.name = name;
    this.spec = spec;
    
    // create AugmentedExpression
    IExpression expression = XPath.createExpression( spec);
    LetExpression let = new LetExpression( expression.getArgument( 0));
    implementation = new RootExpression( let);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return name;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#addArgument(org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public void addArgument( IExpression argument)
  {
    if ( arguments == null) arguments = new ArrayList<IExpression>( 1);
    RootExpression root = new RootExpression( argument);
    LetExpression let = (LetExpression)implementation.getArgument( 0);
    let.addExpression( root, "arg"+arguments.size());
    arguments.add( root);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#removeArgument(org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public void removeArgument( IExpression argument)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    stitchup();
    return implementation.getType();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#getType(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public ResultType getType( IContext context)
  {
    stitchup();
    return implementation.getType( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateBoolean(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    stitchup();
    return implementation.evaluateBoolean( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNodes(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    stitchup();
    return implementation.evaluateNodes( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNumber(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    stitchup();
    return implementation.evaluateNumber( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateString(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public String evaluateString( IContext context) throws ExpressionException
  {
    stitchup();
    return implementation.evaluateString( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#bind(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void bind( IContext context)
  {
    stitchup();
    implementation.addListener( context, implementationListener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#unbind(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void unbind( IContext context)
  {
    stitchup();
    implementation.removeListener( context, implementationListener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    getParent().notifyAdd( this, context, nodes);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    getParent().notifyRemove( this, context, nodes);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, boolean)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    getParent().notifyChange( this, context, newValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, double, double)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    getParent().notifyChange( this, context, newValue, oldValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.lang.String, java.lang.String)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    getParent().notifyChange( this, context, newValue, oldValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context)
  {
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyValue(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext[], org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    getParent().notifyValue( this, contexts, object, newValue, oldValue);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone()
  {
    return cloneOne();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#cloneOne()
   */
  @Override
  protected IExpression cloneOne()
  {
    DelegateFunction clone = new DelegateFunction( name, spec);
    if ( arguments != null)
    {
      for( IExpression argument: arguments) 
        clone.addArgument( (IExpression)argument.clone());
    }
    return clone;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append( getName());
    builder.append( '(');
    if ( arguments != null)
    {
      boolean useComma = arguments.size() > 1;
      for ( int i=0; i<arguments.size(); i++)
      {
        if ( i > 0 && useComma) builder.append( ", ");
        builder.append( arguments.get( i).toString());
      }
    }
    builder.append( ')');
    return builder.toString();
  }

  /**
   * Set the parent IVariableSource of the implementation expression's IVariableSource.
   */
  private void stitchup()
  {
    if ( stitched) return;
    stitched = true;
    
    // set implementation parent variable source
    IVariableSource source = getVariableSource();
    implementation.getVariableSource().setParent( source);

    // arguments should not use implementation variable source for parent
    if ( arguments != null)
      for( IExpression argument: arguments) 
        argument.getVariableSource().setParent( source);
  }
  
  final IExpressionListener implementationListener = new ExpressionListener() {
    public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
    {
      getParent().notifyAdd( DelegateFunction.this, context, nodes);
    }
    public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
    {
      getParent().notifyRemove( DelegateFunction.this, context, nodes);
    }
    public void notifyChange( IExpression expression, IContext context, boolean newValue)
    {
      getParent().notifyChange( DelegateFunction.this, context, newValue);
    }
    public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
    {
      getParent().notifyChange( DelegateFunction.this, context, newValue, oldValue);
    }
    public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
    {
      getParent().notifyChange( DelegateFunction.this, context, newValue, oldValue);
    }
    public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
    {
      getParent().notifyValue( DelegateFunction.this, contexts, object, newValue, oldValue);
    }
    public boolean requiresValueNotification()
    {
      return DelegateFunction.this.requiresValueNotification( DelegateFunction.this);
    }
    public void handleException( IExpression expression, IContext context, Exception e)
    {
      getParent().handleException( DelegateFunction.this, context, e);
    }
  };
  
  String name;
  String spec;
  RootExpression implementation;
  List<IExpression> arguments;
  boolean stitched;
}
