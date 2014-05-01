/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * Expression.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.xpath.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xmodel.GlobalSettings;
import org.xmodel.IChangeSet;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.NullObject;
import org.xmodel.log.Log;
import org.xmodel.xpath.function.BooleanFunction;
import org.xmodel.xpath.function.NumberFunction;
import org.xmodel.xpath.function.StringFunction;
import org.xmodel.xpath.variable.IVariableScope;
import org.xmodel.xpath.variable.IVariableSource;


/**
 * This class provides most of the semantics for IExpression.  Simple expressions need only override the
 * <code>evaluate</code> and <code>returnType</code> methods and one of the methods which returns a 
 * result (e.g. <code>resultNodeSet</code>).  Expressions such as FilteredExpression which have more 
 * present their own context for their arguments must also overload the addListener and removeListener
 * methods to bind their argument listener to the correct context.
 */
public abstract class Expression implements IExpression
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IFunction#addArgument(
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public void addArgument( IExpression argument)
  {
    if ( arguments == null) arguments = new ArrayList<IExpression>( 1);
    arguments.add( argument);
    argument.internal_setParent( this);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#removeArgument(
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public void removeArgument( IExpression argument)
  {
    if ( arguments != null) 
    {
      arguments.remove( argument);
      argument.internal_setParent( null);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#internal_setParent(
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public void internal_setParent( IExpression parent)
  {
    this.parent = parent;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getParent()
   */
  @Override
  public IExpression getParent()
  {
    return parent;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getRoot()
   */
  @Override
  public IExpression getRoot()
  {
    IExpression parent = getParent();
    if ( parent != null) return parent.getRoot();
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#isAncestor(
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public boolean isAncestor( IExpression ancestor)
  {
    IExpression parent = this;
    while( parent != null)
    {
      if ( parent == ancestor) return true;
      parent = parent.getParent();
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#isAbsolute()
   */
  @Override
  public boolean isAbsolute( IContext context)
  {
    if ( arguments != null)
      for( IExpression argument: arguments)
        if ( !argument.isAbsolute( context)) 
          return false;
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getArguments()
   */
  @Override
  public List<IExpression> getArguments()
  {
    if ( arguments == null) return Collections.emptyList();
    return Collections.unmodifiableList( arguments);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getArgument(int)
   */
  @Override
  public IExpression getArgument( int index)
  {
    if ( arguments == null || arguments.size() <= index) return null;
    return arguments.get( index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#setVariable(java.lang.String, java.lang.Boolean)
   */
  @Override
  public void setVariable( String name, Boolean value)
  {
    getLocalScope().set( name, value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#setVariable(java.lang.String, java.lang.Number)
   */
  @Override
  public void setVariable( String name, Number value)
  {
    getLocalScope().set( name, value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#setVariable(java.lang.String, java.lang.String)
   */
  @Override
  public void setVariable( String name, String value)
  {
    getLocalScope().set( name, value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#setVariable(java.lang.String, 
   * org.xmodel.IModelObject)
   */
  @Override
  public void setVariable( String name, IModelObject node)
  {
    getLocalScope().set( name, node);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#setVariable(java.lang.String, java.util.List)
   */
  @Override
  public void setVariable( String name, List<IModelObject> nodes)
  {
    getLocalScope().set( name, nodes);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#setVariable(java.lang.String, 
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public void setVariable( String name, IExpression expression)
  {
    getLocalScope().define( name, expression);
  }

  /**
   * Returns the expression local variable scope (for defining variables).
   * @return Returns the expression local variable scope.
   */
  public IVariableScope getLocalScope()
  {
    return getVariableSource().getScope( "local");
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getVariableSource()
   */
  @Override
  public IVariableSource getVariableSource()
  {
    IExpression root = getRoot();
    if ( root != this) return root.getVariableSource();
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public ResultType getType( IContext context)
  {
    // the type of most expressions does not depend on the context scope
    return getType();
  }
  
  /**
   * Assert that all arguments have the specified type.
   * @param context The context of the evaluation.
   * @param type The type of the argument.
   */
  protected void assertType( IContext context, ResultType type) throws ExpressionException
  {
    if ( arguments != null)
    {
      for ( int i=0; i<arguments.size(); i++)
        assertType( context, i, type);
    }
  }
  
  /**
   * Assert that the argument with the specified index has the specified type.
   * @param context The context of the evaluation.
   * @param index The index of the argument.
   * @param type The type of the argument.
   */
  protected void assertType( IContext context, int index, ResultType type) throws ExpressionException
  {
    IExpression argument = getArgument( index);
    if ( argument != null)
    {
      ResultType actualType = argument.getType( context);
      if ( actualType == ResultType.UNDEFINED)
      {
        throw new ExpressionException( this,
          "Undefined variable in argument: "+argument);
      }
      
      if ( type != actualType)
      {
        throw new ExpressionException( this, 
          "Illegal argument type: "+actualType+" in expression: "+toString());
      }
    }
  }
  
  /**
   * Assert that the number of arguments falls in the specified range.
   * @param min The minimum number of arguments.
   * @param max The maximum number of arguments.
   */
  protected void assertArgs( int min, int max) throws ExpressionException
  {
    List<IExpression> arguments = getArguments();
    if ( arguments.size() < min || (max >= 0 && arguments.size() > max))
    {
      throw new ExpressionException( this, "Error: wrong number of arguments: "+toString());
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#requiresContext()
   */
  @Override
  public boolean requiresOrdinalContext()
  {
    if ( arguments != null)
      for( IExpression argument: arguments)
        if ( argument.requiresOrdinalContext()) return true;
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateBoolean()
   */
  @Override
  public boolean evaluateBoolean() throws ExpressionException
  {
    return evaluateBoolean( new Context( new NullObject()));
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateNodes()
   */
  @Override
  public List<IModelObject> evaluateNodes() throws ExpressionException
  {
    return evaluateNodes( new Context( new NullObject()));
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateNumber()
   */
  @Override
  public double evaluateNumber() throws ExpressionException
  {
    return evaluateNumber( new Context( new NullObject()));
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateString()
   */
  @Override
  public String evaluateString() throws ExpressionException
  {
    return evaluateString( new Context( new NullObject()));
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateNodes(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    throw new ExpressionException( this, "Expression does not return node-set.");
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateNumber(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    switch( getType( context))
    {
      case NODES:   return NumberFunction.numericValue( evaluateNodes( context));
      case STRING:  return NumberFunction.numericValue( evaluateString( context));
      case BOOLEAN: return NumberFunction.numericValue( evaluateBoolean( context));
    }
    throw new ExpressionException( this, "Expression implementation error.");
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateString(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public String evaluateString( IContext context) throws ExpressionException
  {
    switch( getType( context))
    {
      case NODES:   return StringFunction.stringValue( evaluateNodes( context));
      case NUMBER:  return StringFunction.stringValue( evaluateNumber( context));
      case BOOLEAN: return StringFunction.stringValue( evaluateBoolean( context));
    }
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateBoolean(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    switch( getType( context))
    {
      case NODES:  return BooleanFunction.booleanValue( evaluateNodes( context));
      case NUMBER: return BooleanFunction.booleanValue( evaluateNumber( context));
      case STRING: return BooleanFunction.booleanValue( evaluateString( context));
    }
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateNodes(
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public List<IModelObject> evaluateNodes( IContext context, List<IModelObject> defaultResult)
  {
    try
    {
      return evaluateNodes( context);
    }
    catch( ExpressionException e)
    {
      return defaultResult;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateNumber(
   * org.xmodel.xpath.expression.IContext, double)
   */
  @Override
  public double evaluateNumber( IContext context, double defaultResult)
  {
    try
    {
      return evaluateNumber( context);
    }
    catch( ExpressionException e)
    {
      return defaultResult;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateString(
   * org.xmodel.xpath.expression.IContext, java.lang.String)
   */
  @Override
  public String evaluateString( IContext context, String defaultResult)
  {
    try
    {
      return evaluateString( context);
    }
    catch( ExpressionException e)
    {
      return defaultResult;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateBoolean(
   * org.xmodel.xpath.expression.IContext, boolean)
   */
  @Override
  public boolean evaluateBoolean( IContext context, boolean defaultResult)
  {
    try
    {
      return evaluateBoolean( context);
    }
    catch( ExpressionException e)
    {      log.exception( e);
      return defaultResult;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#createSubtree(org.xmodel.xpath.expression.IContext, org.xmodel.IModelObjectFactory, org.xmodel.IChangeSet, java.lang.Object)
   */
  @Override
  public void createSubtree( IContext context, IModelObjectFactory factory, IChangeSet undo, Object setter, boolean leafOnly)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#query(java.util.List)
   */
  @Override
  public List<IModelObject> query( List<IModelObject> result)
  {
    return query( new Context( new NullObject()), result);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#queryFirst()
   */
  @Override
  public IModelObject queryFirst()
  {
    return queryFirst( new Context( new NullObject()));
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#query(org.xmodel.IModelObject, java.util.List)
   */
  @Override
  public List<IModelObject> query( IModelObject object, List<IModelObject> result)
  {
    try 
    {
      List<IModelObject> nodes = evaluateNodes( new Context( object));
      if ( nodes == null) return Collections.emptyList();
      if ( result == null) return nodes;
      result.addAll( nodes);
      return result;
    }
    catch( ExpressionException e)
    {
      throw new IllegalStateException( e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#queryFirst(org.xmodel.IModelObject)
   */
  @Override
  public IModelObject queryFirst( IModelObject object)
  {
    try
    {
      List<IModelObject> nodes = evaluateNodes( new Context( object));
      if ( nodes == null || nodes.size() == 0) return null;
      return nodes.get( 0);
    }
    catch( ExpressionException e)
    {
      throw new IllegalStateException( e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#query(org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public List<IModelObject> query( IContext context, List<IModelObject> result)
  {
    try
    {
      List<IModelObject> nodes = evaluateNodes( context);
      if ( nodes == null) return Collections.emptyList();
      if ( result == null) return nodes;
      result.addAll( nodes);
      return result;
    }
    catch( ExpressionException e)
    {
      throw new IllegalStateException( e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#queryFirst(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public IModelObject queryFirst( IContext context)
  {
    try
    {
      List<IModelObject> nodes = evaluateNodes( context);
      if ( nodes == null || nodes.size() == 0) return null;
      return nodes.get( 0);
    }
    catch( ExpressionException e)
    {
      throw new IllegalStateException( e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#bind(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void bind( IContext context)
  {
    if ( arguments != null)
      for( IExpression argument: arguments)
        argument.bind( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#unbind(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void unbind( IContext context)
  {
    if ( arguments != null)
      for( IExpression argument: arguments)
        argument.unbind( context);
  }
  
  /**
   * Rebind this expression with the specified context. This method first reverts the current 
   * update and calls unbind.  Then it restores the update and calls bind.  If there is no
   * update in progress this method has no effect.
   * @param context The context.
   */
  public void rebind( IContext context)
  {
    IModel model = GlobalSettings.getInstance().getModel();
    model.revert();
    unbind( context);
    model.restore();
    bind( context);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#addListener(
   * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.expression.IExpressionListener)
   */
  @Override
  public void addListener( IContext context, IExpressionListener listener)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#removeListener(
   * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.expression.IExpressionListener)
   */
  @Override
  public void removeListener( IContext context, IExpressionListener listener)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#addNotifyListener(
   * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.expression.IExpressionListener)
   */
  @Override
  public void addNotifyListener( IContext context, IExpressionListener listener)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#removeNotifyListener(
   * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.expression.IExpressionListener)
   */
  @Override
  public void removeNotifyListener( IContext context, IExpressionListener listener)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getListeners()
   */
  @Override
  public ExpressionListenerList getListeners()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, boolean)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, double, double)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.String, java.lang.String)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context)
  {
    if ( parent != null) parent.notifyChange( this, context);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#handleException(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.Exception)
   */
  @Override
  public void handleException( IExpression expression, IContext context, Exception e)
  {
    if ( parent != null) parent.handleException( expression, context, e);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#requiresValueNotification(
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public boolean requiresValueNotification( IExpression argument)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#notifyValue(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    if ( parent != null) parent.notifyValue( this, contexts, object, newValue, oldValue);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone()
  {
    IExpression clone = cloneOne();
    List<IExpression> arguments = getArguments();
    if ( arguments != null)
    {
      for( IExpression argument: arguments)
        clone.addArgument( (IExpression)argument.clone());
    }
    return clone;
  }
  
  /**
   * Return a clone of just this expression.
   * @return Returns a clone of just this expression.
   */
  protected IExpression cloneOne()
  {
    try
    {
      return (IExpression)getClass().newInstance();
    }
    catch( Exception e)
    {
      log.exception( e);
      return null;
    }
  }
  
  IExpression parent;
  List<IExpression> arguments;
  
  private static Log log = Log.getLog( "org.xmodel.xpath.expression");
}
