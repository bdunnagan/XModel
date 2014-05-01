/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ForExpression.java
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xmodel.GlobalSettings;
import org.xmodel.IChangeSet;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.memento.IMemento;
import org.xmodel.memento.VariableMemento;
import org.xmodel.xpath.expression.IExpression.ResultType;
import org.xmodel.xpath.variable.IVariableListener;
import org.xmodel.xpath.variable.IVariableScope;
import org.xmodel.xpath.variable.IVariableSource;
import org.xmodel.xpath.variable.Precedences;
import org.xmodel.xpath.variable.VariableScope;


/**
 * A partial implementation of the X-Path 2.0 for/return expression. Since this is a hybrid
 * X-Path 1.0/2.0 implementation, both the iterator and the return expressions must return
 * node-sets. Also, this expression does not behave as expected when the iterator variable
 * is enclosed in the string() or number() functions and this usage should be avoided.
 */
public class ForExpression extends Expression
{
  public ForExpression( String variable)
  {
    this.variable = variable;
    this.forScope = new VariableScope( "for", Precedences.forScope);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "for-return";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NODES;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNodes(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    assertType( context, 0, ResultType.NODES);

    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);

    IVariableSource source = getVariableSource();
    try
    {
      source.addScope( forScope);
      List<IModelObject> nodes = arg0.evaluateNodes( context);
      List<IModelObject> result = new ArrayList<IModelObject>();
      for( IModelObject node: nodes)
      {
        forScope.set( variable, node);
        assertType( context, 1, ResultType.NODES);
        result.addAll( arg1.evaluateNodes( context));
      }
      return result;
    }
    finally
    {
      source.removeScope( forScope);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#createSubtree(org.xmodel.xpath.expression.IContext, org.xmodel.IModelObjectFactory, org.xmodel.IChangeSet, java.lang.Object)
   */
  @Override
  public void createSubtree( IContext context, IModelObjectFactory factory, IChangeSet undo, Object setter, boolean leafOnly)
  {
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    List<IModelObject> nodes = arg0.evaluateNodes( context);
    for( int i=0; i<nodes.size(); i++)
    {
      ReturnContext returnContext = new ReturnContext( context, variable, nodes.get( i));
      arg1.createSubtree( returnContext, factory, undo, setter, leafOnly);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#bind(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void bind( IContext context)
  {
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    
    // bind iterator expression
    arg0.bind( context);
    
    // bind return expression
    try
    {
      List<IModelObject> nodes = arg0.evaluateNodes( context);
      for( IModelObject node: nodes)
      {
        ReturnContext returnContext = new ReturnContext( context, variable, node);
        arg1.bind( returnContext);
      }
    }
    catch( ExpressionException e)
    {
      if ( parent != null) parent.handleException( this, context, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#unbind(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void unbind( IContext context)
  {
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    
    // unbind iterator expression
    arg0.unbind( context);
    
    // unbind return expression
    try
    {
      List<IModelObject> nodes = arg0.evaluateNodes( context);
      for( IModelObject node: nodes)
      {
        ReturnContext returnContext = new ReturnContext( context, variable, node);
        arg1.unbind( returnContext);
      }
    }
    catch( ExpressionException e)
    {
      if ( parent != null) parent.handleException( this, context, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    if ( expression == arg0)
    {
      for( IModelObject node: nodes)
      {
        ReturnContext returnContext = new ReturnContext( context, variable, node);
        try
        {
          arg1.bind( returnContext);
          List<IModelObject> result = arg1.evaluateNodes( returnContext);
          if ( result.size() > 0) parent.notifyAdd( this, context, result);
        }
        catch( ExpressionException e)
        {
          parent.handleException( this, context, e);
        }
      }
    }
    else if ( expression == arg1)
    {
      if ( parent != null) parent.notifyAdd( this, context.getParent(), nodes);
    }
    else
    {
      throw new IllegalStateException( "Notification from expression which is not an argument.");
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    if ( expression == arg0)
    {
      IModel model = GlobalSettings.getInstance().getModel();
      for( IModelObject node: nodes)
      {
        model.revert();
        ReturnContext returnContext = new ReturnContext( context, variable, node);
        try
        {
          arg1.unbind( returnContext);
          List<IModelObject> result = arg1.evaluateNodes( returnContext);
          model.restore();
          if ( parent != null && result.size() > 0) parent.notifyRemove( this, context, result);
        }
        catch( ExpressionException e)
        {
          if ( parent != null) parent.handleException( this, context, e);
        }
      }
    }
    else if ( expression == arg1)
    {
      if ( parent != null) parent.notifyRemove( this, context.getParent(), nodes);
    }
    else
    {
      throw new IllegalStateException( "Notification from expression which is not an argument.");
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context)
  {
    IModel model = GlobalSettings.getInstance().getModel();
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    
    model.revert();
    Collection<IModelObject> oldNodes = arg0.evaluateNodes( context, null);
    if ( oldNodes.size() > 5) oldNodes = new HashSet<IModelObject>( oldNodes);
    
    model.restore();
    Collection<IModelObject> newNodes = arg0.evaluateNodes( context, null);
    if ( newNodes.size() > 5) newNodes = new HashSet<IModelObject>( newNodes);

    // unbind removed nodes
    model.revert();
    for( IModelObject node: oldNodes)
      if ( !newNodes.contains( node))
      {
        ReturnContext returnContext = new ReturnContext( context, variable, node);
        arg1.unbind( returnContext);
      }
    
    // bind added nodes
    model.restore();
    for( IModelObject node: newNodes)
      if ( !oldNodes.contains( node))
      {
        ReturnContext returnContext = new ReturnContext( context, variable, node);
        arg1.bind( returnContext);
      }

    // foward notification
    if ( parent != null)
    {
      if ( expression == arg1) context = context.getParent();
      parent.notifyChange( this, context);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyValue(d
   * unnagan.bob.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext[], 
   * org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    if ( expression == getArgument( 0))
    {
      super.notifyValue( expression, contexts, object, newValue, oldValue);
    }
    else
    {
      IContext[] parents = new IContext[ contexts.length];
      for( int i=0; i<parents.length; i++) parents[ i] = contexts[ i].getParent();
      super.notifyValue( expression, parents, object, newValue, oldValue);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#requiresValueNotification(
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public boolean requiresValueNotification( IExpression argument)
  {
    // always require value notification from the iterator expression since we cannot
    // determine here how the variable is used in the return expression
    if ( argument == getArgument( 0)) return true;
    
    // require value notification from the return expression if the parent requires value notification
    if ( parent.requiresValueNotification( this) && argument == getArgument( 1)) return true;
    return false;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#cloneOne()
   */
  @Override
  protected IExpression cloneOne()
  {
    return new ForExpression( variable);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append( "for ");
    builder.append( '$');
    builder.append( variable);
    builder.append( " in ");
    builder.append( getArgument( 0));
    builder.append( " return\n");
    builder.append( "  ");
    builder.append( getArgument( 1));
    return builder.toString();
  }
  
  String variable;
  IExpression returnExprClone;
  IVariableScope forScope;
}

/**
 * A context used to bind each node in the iterator node-set to the return expression.
 * This context should be rewritten without Context for a base-class since it has the
 * overhead of the <code>updates</code> table.  Note that the bound variable never 
 * changes so variable notification isn't necessary.
 */
class ReturnContext extends Context
{
  /**
   * Create a ReturnContext for the specified ForExpression context.
   * @param context The ForExpression input context.
   * @param name The name of the iterator variable.
   * @param node The iteration node.
   */
  public ReturnContext( IContext context, String name, IModelObject node)
  {
    super( new ReturnScope( context.getScope(), name, node), context.getObject(), context.getPosition(), context.getSize());
    parent = context;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Context#getParent()
   */
  @Override
  public IContext getParent()
  {
    return parent;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Context#get(java.lang.String)
   */
  @Override
  public Object get( String name)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Context#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object arg0)
  {
    if ( arg0 instanceof ReturnContext)
    {
      ReturnContext context = (ReturnContext)arg0;
      return parent.equals( context.getParent()) && context.scope.equals( scope);
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Context#hashCode()
   */
  @Override
  public int hashCode()
  {
    return super.hashCode() ^ scope.hashCode();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Context#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "return:");
    sb.append( super.toString());
    return sb.toString();
  }

  IContext parent;
}

/**
 * An implementation of IVariableScope which resolves the iterator variable for the expression to which
 * it belongs and resolves all other variables with an optional parent context scope. ReturnScope does
 * not bind its variable.
 */
class ReturnScope implements IVariableScope
{
  /**
   * Create a ReturnScope to hold the specified variable.
   * @param parent The parent scope.
   * @param name The name of the variable.
   * @param node The value of the variable.
   */
  ReturnScope( IVariableScope parent, String name, IModelObject node)
  {
    this.parent = parent;
    this.name = name;
    this.value = Collections.singletonList( node);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#getName()
   */
  public String getName()
  {
    return "context";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#getPrecedence()
   */
  public int getPrecedence()
  {
    return Precedences.contextScope;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#getVariables()
   */
  @Override
  public Collection<String> getVariables()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#set(java.lang.String, java.lang.Boolean)
   */
  public Boolean set( String name, Boolean value)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#set(java.lang.String, org.xmodel.IModelObject)
   */
  public List<IModelObject> set( String name, IModelObject value)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#set(java.lang.String, java.util.List)
   */
  public List<IModelObject> set( String name, List<IModelObject> value)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#set(java.lang.String, java.lang.Object)
   */
  @Override
  public Object set( String name, Object value)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#insert(java.lang.String, java.lang.Object)
   */
  @Override
  public void insert( String name, Object object)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#insert(java.lang.String, java.lang.Object, int)
   */
  @Override
  public void insert( String name, Object object, int index)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#remove(java.lang.String, java.lang.Object)
   */
  @Override
  public void remove( String name, Object object)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#remove(java.lang.String, int)
   */
  @Override
  public void remove( String name, int index)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#set(java.lang.String, java.lang.Number)
   */
  public Number set( String name, Number value)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#set(java.lang.String, java.lang.String)
   */
  public String set( String name, String value)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#setPojo(java.lang.String, java.lang.Object, org.xmodel.IModelObjectFactory)
   */
  public Object setPojo( String name, Object pojo, IModelObjectFactory factory)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#define(java.lang.String, org.xmodel.xpath.expression.IExpression)
   */
  public void define( String name, IExpression expression)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#clear(java.lang.String)
   */
  @Override
  public void clear( String name)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#get(java.lang.String, org.xmodel.xpath.expression.IContext)
   */
  public Object get( String name, IContext context) throws ExpressionException
  {
    if ( name.equals( this.name)) return value;
    if ( parent == null) return null;
    return parent.get( name, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#get(java.lang.String)
   */
  public Object get( String name)
  {
    if ( name.equals( this.name)) return value;
    if ( parent == null) return null;
    return parent.get( name);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#getAll()
   */
  public Collection<String> getAll()
  {
    Set<String> names = new HashSet<String>();
    if ( parent != null) names.addAll( parent.getAll());
    names.add( name);
    return names;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#getSource()
   */
  public IVariableSource getSource()
  {
    return source;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#getType(java.lang.String)
   */
  public ResultType getType( String name)
  {
    if ( name.equals( this.name)) return ResultType.NODES;
    return parent.getType( name);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#getType(java.lang.String, org.xmodel.xpath.expression.IContext)
   */
  public ResultType getType( String name, IContext context)
  {
    if ( name.equals( this.name)) return ResultType.NODES;
    return parent.getType( name, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#internal_setSource(org.xmodel.xpath.variable.IVariableSource)
   */
  public void internal_setSource( IVariableSource source)
  {
    this.source = source;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#isDefined(java.lang.String)
   */
  public boolean isDefined( String name)
  {
    if ( name.equals( this.name)) return true;
    if ( parent == null) return false;
    return parent.isDefined( name);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#isBound(java.lang.String)
   */
  public boolean isBound( String name)
  {
    if ( name.equals( this.name)) return false;
    if ( parent == null) return false;
    return parent.isBound( name);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#addListener(java.lang.String, 
   * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.variable.IVariableListener)
   */
  public void addListener( String name, IContext context, IVariableListener listener)
  {
    if ( name.equals( this.name)) return;
    parent.addListener( name, context, listener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#removeListener(java.lang.String, 
   * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.variable.IVariableListener)
   */
  public void removeListener( String name, IContext context, IVariableListener listener)
  {
    if ( name.equals( this.name)) return;
    parent.removeListener( name, context, listener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#restore(org.xmodel.memento.IMemento)
   */
  public void restore( IMemento iMemento)
  {
    VariableMemento memento = (VariableMemento)iMemento;
    if ( memento.varName.equals( name)) return;
    parent.restore( iMemento);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#revert(org.xmodel.memento.IMemento)
   */
  public void revert( IMemento iMemento)
  {
    VariableMemento memento = (VariableMemento)iMemento;
    if ( memento.varName.equals( name)) return;
    parent.revert( iMemento);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#copyFrom(org.xmodel.xpath.variable.IVariableScope)
   */
  public void copyFrom( IVariableScope scope)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object arg0)
  {
    if ( arg0 instanceof ReturnScope)
    {
      ReturnScope scope = (ReturnScope)arg0;
      return scope.name.equals( name) && scope.value.equals( value);
    }
    return false;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    return name.hashCode() ^ value.hashCode();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#cloneOne()
   */
  public IVariableScope cloneOne()
  {
    throw new UnsupportedOperationException();
  }
  
  IVariableSource source;
  IVariableScope parent;
  String name;
  Object value;
}