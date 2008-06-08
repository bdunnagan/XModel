/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.expression;

import java.util.ArrayList;
import java.util.List;

import dunnagan.bob.xmodel.IChangeSet;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IModelObjectFactory;

/**
 * An expression consisting of a base expression and one or more additional expressions which are 
 * associated with variable names.  During evaluation and binding the additional expressions are
 * evaluated and bound separately and their results are stored in their associated variables on 
 * a local, stateful context which is introduced into the context chain.  The base expression is
 * then evaluated and has access to the current value of the variables.<br/>
 * The variable assignment clauses in the expression are evaluated in order.
 */
public class LetExpression extends Expression
{
  /**
   * Create an LetExpression without an argument. The argument must be defined before
   * the expression is used.
   */
  public LetExpression()
  {
    clauses = new ArrayList<Clause>();
  }
  
  /**
   * Create an LetExpression based on the specified expression.
   * @param expression The base expression.
   */
  public LetExpression( IExpression expression)
  {
    this();
    addArgument( expression);
  }
  
  /**
   * Define a variable/expression pair.
   * @param expression The expression.
   * @param variable The variable.
   */
  public void addExpression( RootExpression expression, String variable)
  {
    clauses.add( new Clause( variable, expression));
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "let";
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return getArgument( 0).getType();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#getType(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public ResultType getType( IContext context)
  {
    LocalContext local = new LocalContext( context);
    updateVariables( local, 0);
    return getArgument( 0).getType( local);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#evaluateBoolean(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    LocalContext local = new LocalContext( context);
    updateVariables( local, 0);
    return getArgument( 0).evaluateBoolean( local);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#evaluateNodes(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    LocalContext local = new LocalContext( context);
    updateVariables( local, 0);
    return getArgument( 0).evaluateNodes( local);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#evaluateNumber(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    LocalContext local = new LocalContext( context);
    updateVariables( local, 0);
    return getArgument( 0).evaluateNumber( local);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#evaluateString(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public String evaluateString( IContext context) throws ExpressionException
  {
    LocalContext local = new LocalContext( context);
    updateVariables( local, 0);
    return getArgument( 0).evaluateString( local);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#createSubtree(dunnagan.bob.xmodel.xpath.expression.IContext, dunnagan.bob.xmodel.IModelObjectFactory, dunnagan.bob.xmodel.IChangeSet)
   */
  @Override
  public void createSubtree( IContext context, IModelObjectFactory factory, IChangeSet undo)
  {
    LocalContext local = new LocalContext( context);
    updateVariables( local, 0);
    getArgument( 0).createSubtree( local, factory, undo);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#bind(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public void bind( IContext context)
  {
    LocalContext local = new LocalContext( context);
    
    // update variables
    updateVariables( local, 0);
    
    // bind additional expressions
    for( Clause clause: clauses)
      clause.expression.addListener( local, expressionListener);

    // bind base expression
    getArgument( 0).bind( local);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#unbind(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public void unbind( IContext context)
  {
    LocalContext local = new LocalContext( context);
    
    // update variables
    updateVariables( local, 0);
    
    // unbind base expression
    getArgument( 0).unbind( local);
    
    // unbind additional expressions
    for( Clause clause: clauses)
      clause.expression.removeListener( local, expressionListener);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyAdd(dunnagan.bob.xmodel.xpath.expression.IExpression, 
   * dunnagan.bob.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    parent.notifyAdd( this, context.getParent(), nodes);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyRemove(dunnagan.bob.xmodel.xpath.expression.IExpression, 
   * dunnagan.bob.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    parent.notifyRemove( this, context.getParent(), nodes);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(dunnagan.bob.xmodel.xpath.expression.IExpression, 
   * dunnagan.bob.xmodel.xpath.expression.IContext, boolean)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    parent.notifyChange( this, context.getParent(), newValue);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(dunnagan.bob.xmodel.xpath.expression.IExpression, 
   * dunnagan.bob.xmodel.xpath.expression.IContext, double, double)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    parent.notifyChange( this, context.getParent(), newValue, oldValue);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(dunnagan.bob.xmodel.xpath.expression.IExpression, 
   * dunnagan.bob.xmodel.xpath.expression.IContext, java.lang.String, java.lang.String)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    parent.notifyChange( this, context.getParent(), newValue, oldValue);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(dunnagan.bob.xmodel.xpath.expression.IExpression, 
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context)
  {
    parent.notifyChange( this, context.getParent());
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyValue(dunnagan.bob.xmodel.xpath.expression.IExpression, 
   * dunnagan.bob.xmodel.xpath.expression.IContext[], dunnagan.bob.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    for( int i=0; i<contexts.length; i++) contexts[ i] = contexts[ i].getParent();
    parent.notifyValue( this, contexts, object, newValue, oldValue);
  }

  /**
   * Evaluate clauses and assign variable starting with the clause at the specified index.
   * @param context The local context.
   * @param index The index of the first clause to evaluate.
   */
  private void updateVariables( LocalContext context, int index)
  {
    for( int i=index; i<clauses.size(); i++)
    {
      Clause clause = clauses.get( i);
      switch( clause.expression.getType( context))
      {
        case NODES:
          context.set( clause.variable, clause.expression.evaluateNodes( context));
          break;
        
        case NUMBER:
          context.set( clause.variable, clause.expression.evaluateNumber( context));
          break;
          
        case STRING:
          context.set( clause.variable, clause.expression.evaluateString( context));
          break;
          
        case BOOLEAN:
          context.set( clause.variable, clause.expression.evaluateBoolean( context));
          break;
      }
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    for( Clause clause: clauses)
    {
      builder.append( "let $");
      builder.append( clause.variable);
      builder.append( " := ");
      builder.append( clause.expression);
      builder.append( ";\n");
    }
    
    builder.append( getArgument( 0));
    return builder.toString();
  }

  private IExpressionListener expressionListener = new ExpressionListener() {
    public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
    {
      // rebinding is necessary since some expressions, such as filter expressions, will use the
      // let variable on the left-hand-side and must be rebound when that variable changes
      rebind( context.getParent());
      LocalContext local = (LocalContext)context;
      updateVariables( local, 0);
      LetExpression.this.parent.notifyChange( LetExpression.this, context.getParent());
    }
    public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
    {
      // rebinding is necessary since some expressions, such as filter expressions, will use the
      // let variable on the left-hand-side and must be rebound when that variable changes
      rebind( context.getParent());
      LocalContext local = (LocalContext)context;
      updateVariables( local, 0);
      LetExpression.this.parent.notifyChange( LetExpression.this, context.getParent());
    }
    public void notifyChange( IExpression expression, IContext context, boolean newValue)
    {
      // rebinding is necessary since some expressions, such as filter expressions, will use the
      // let variable on the left-hand-side and must be rebound when that variable changes
      rebind( context.getParent());
      LocalContext local = (LocalContext)context;
      updateVariables( local, 0);
      LetExpression.this.parent.notifyChange( LetExpression.this, context.getParent());
    }
    public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
    {
      // rebinding is necessary since some expressions, such as filter expressions, will use the
      // let variable on the left-hand-side and must be rebound when that variable changes
      rebind( context.getParent());
      LocalContext local = (LocalContext)context;
      updateVariables( local, 0);
      LetExpression.this.parent.notifyChange( LetExpression.this, context.getParent());
    }
    public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
    {
      // rebinding is necessary since some expressions, such as filter expressions, will use the
      // let variable on the left-hand-side and must be rebound when that variable changes
      rebind( context.getParent());
      LocalContext local = (LocalContext)context;
      updateVariables( local, 0);
      LetExpression.this.parent.notifyChange( LetExpression.this, context.getParent());
    }
    public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
    {
      // rebinding is necessary since some expressions, such as filter expressions, will use the
      // let variable on the left-hand-side and must be rebound when that variable changes
      for( IContext context: contexts)
      {
        rebind( context.getParent());
        LocalContext local = (LocalContext)context;
        updateVariables( local, 0);
        LetExpression.this.parent.notifyChange( LetExpression.this, context.getParent());
      }
    }
    public boolean requiresValueNotification()
    {
      return true;
    }
  };

  public class Clause
  {
    public Clause( String variable, IExpression expression)
    {
      this.variable = variable;
      this.expression = expression;
    }
    String variable;
    IExpression expression;
  }

  private List<Clause> clauses;
}
