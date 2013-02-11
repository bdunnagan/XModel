/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * Context.java
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.xmodel.GlobalSettings;
import org.xmodel.IModelObject;
import org.xmodel.Update;
import org.xmodel.xpath.variable.IVariableScope;
import org.xmodel.xpath.variable.Precedences;

/**
 * An implementation of IContext for top-level contexts. When binding (adding a listener to) a context 
 * of this class it is not necessary to pass the same instance to the unbind since the <code>equals</code>
 * and <code>hashCode</code> methods operate on the context object, position and size. This implementation
 * does not support variable assignments which necessitate unbinding with the same instance of IContext
 * with which the bind was performed. The StatefulContext class does not have this limitation.
 * <p>
 * Consider the case where two instances of an IContext implementation compare equal. The target of a 
 * particular notification is identified by three pieces of information: the context, the expression 
 * and the listener. If the same expression and listener are bound to two contexts which compare equal
 * then updating variables on one context will result in notification to both contexts.
 */
public class Context implements IContext
{
  /**
   * Create a context for the given context node with position and size equal to one.
   * @param object The context node.
   */
  public Context( IModelObject object)
  {
    this( object, 1, 1);
  }
  
  /**
   * Create a context for the given context node.
   * @param scope The scope to associate with this context.
   * @param object The context node.
   * @param position The context position.
   * @param size The context size.
   */
  public Context( IModelObject object, int position, int size)
  {
    this.object = object;
    this.position = position;
    this.size = size;
    this.updates = new HashMap<IExpression, Update>( 1);
  }
    
  /**
   * Create a context for the given context node.
   * @param scope The scope to associate with this context.
   * @param object The context node.
   * @param position The context position.
   * @param size The context size.
   */
  protected Context( IVariableScope scope, IModelObject object, int position, int size)
  {
    this.object = object;
    this.position = position;
    this.size = size;
    this.updates = new HashMap<IExpression, Update>( 1);
    this.scope = scope;
  }
    
  /**
   * Create a context for the given context node with position and size equal to one.
   * @param object The context node.
   */
  public Context( IContext scope, IModelObject object)
  {
    this( (scope != null)? scope.getScope(): null, object, 1, 1);
  }
  
  /**
   * Create a context for the given context node.
   * @param scope The scope to associate with this context.
   * @param object The context node.
   * @param position The context position.
   * @param size The context size.
   */
  public Context( IContext scope, IModelObject object, int position, int size)
  {
    this( (scope != null)? scope.getScope(): null, object, position, size);
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

//  /* (non-Javadoc)
//   * @see org.xmodel.xpath.expression.IContext#getModel()
//   */
//  public IModel getModel()
//  {
//    return GlobalSettings.getInstance().getModel();
//  }
//
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#getParent()
   */
  public IContext getParent()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#getRoot()
   */
  public IContext getRoot()
  {
    return this;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#getContextNode()
   */
  public IModelObject getObject()
  {
    return object;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#getPosition()
   */
  public int getPosition()
  {
    return position;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#getSize()
   */
  public int getSize()
  {
    return size;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#set(java.lang.String, java.lang.Boolean)
   */
  public Boolean set( String name, Boolean value)
  {
    if ( scope != null) return scope.set( name, value);
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#set(java.lang.String, org.xmodel.IModelObject)
   */
  public List<IModelObject> set( String name, IModelObject value)
  {
    if ( scope != null) return scope.set( name, value);
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#set(java.lang.String, java.util.List)
   */
  public List<IModelObject> set( String name, List<IModelObject> value)
  {
    if ( scope != null) return scope.set( name, value);
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#set(java.lang.String, java.lang.Number)
   */
  public Number set( String name, Number value)
  {
    if ( scope != null) return scope.set( name, value);
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#set(java.lang.String, java.lang.String)
   */
  public String set( String name, String value)
  {
    if ( scope != null) return scope.set( name, value);
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#get(java.lang.String)
   */
  public Object get( String name)
  {
    if ( scope != null) return scope.get( name);
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#setExecutor(java.util.concurrent.Executor)
   */
  @Override
  public void setExecutor( Executor executor)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#getExecutor()
   */
  @Override
  public Executor getExecutor()
  {
    return GlobalSettings.getInstance().getDefaultExecutor();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#notifyBind(org.xmodel.xpath.expression.IExpression)
   */
  public void notifyBind( IExpression expression)
  {
    updates.remove( expression);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#notifyUnbind(org.xmodel.xpath.expression.IExpression)
   */
  public void notifyUnbind( IExpression expression)
  {
    updates.remove( expression);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#notifyUpdate(org.xmodel.xpath.expression.IExpression)
   */
  public void notifyUpdate( IExpression expression)
  {
    updates.put( expression, GlobalSettings.getInstance().getModel().getCurrentUpdate());
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#markUpdate(org.xmodel.xpath.expression.IExpression)
   */
  public void markUpdate( IExpression expression)
  {
    updates.remove( expression);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#shouldUpdate(org.xmodel.xpath.expression.IExpression)
   */
  public boolean shouldUpdate( IExpression expression)
  {
    Update lastUpdate = updates.get( expression);
    return lastUpdate == null || !lastUpdate.isActive() || lastUpdate != GlobalSettings.getInstance().getModel().getCurrentUpdate();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#getLastUpdate(org.xmodel.xpath.expression.IExpression)
   */
  public Update getLastUpdate( IExpression expression)
  {
    return updates.get( expression);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#getScope()
   */
  public IVariableScope getScope()
  {
    return scope;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#cloneOne()
   */
  public IVariableScope cloneOne()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#getLock()
   */
  @Override
  public synchronized ReadWriteLock getLock()
  {
    if ( lock != null) return lock;
    lock = new ReentrantReadWriteLock();
    return lock;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    int hashCode = object.hashCode();
    hashCode ^= position + (size << 16);
    return hashCode;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals( Object arg0)
  {
    if ( !(arg0 instanceof IContext)) return false;
    IContext rhs = (IContext)arg0;
    if ( rhs.getObject() != getObject()) return false;
    if ( rhs.getPosition() != getPosition()) return false;
    if ( rhs.getSize() != getSize()) return false;
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "[ "); sb.append( position);
    sb.append( ", "); sb.append( size);
    sb.append( ", "); sb.append( object);
    sb.append( "]");
    return sb.toString();
  }
  
  private IModelObject object;
  private int position;
  private int size;
  private Map<IExpression, Update> updates;
  protected IVariableScope scope;
  private ReadWriteLock lock;
}