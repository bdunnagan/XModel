/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SubContext.java
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

import java.util.List;
import java.util.concurrent.Executor;

import org.xmodel.GlobalSettings;
import org.xmodel.IModelObject;
import org.xmodel.Update;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * An implementation of IContext for nested contexts within an expression. Nested contexts share
 * the same variable scope as their root context.
 */
public class SubContext implements IContext
{
  /**
   * Create a copy of the specified context.
   * @param parent The parent.
   * @param context The context.
   */
  public SubContext( IContext parent, IContext context)
  {
    this( parent, context.getObjects());
  }
  
  /**
   * Create a context for the given context objects.
   * @param parent The parent of this context.
   * @param objects The context objects.
   */
  public SubContext( IContext parent, List<Object> objects)
  {
    if ( parent == null) throw new IllegalArgumentException( "SubContext must have parent.");
    this.parent = parent;
    this.objects = objects;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#getParent()
   */
  public IContext getParent()
  {
    return parent;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#getRoot()
   */
  public IContext getRoot()
  {
    IContext root = this;
    while( true)
    {
      if ( root.getParent() == null) return root;
      root = root.getParent();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#getObjects()
   */
  @Override
  public List<Object> getObjects()
  {
    return objects;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#set(java.lang.String, java.lang.Boolean)
   */
  public Boolean set( String name, Boolean value)
  {
    IVariableScope scope = getScope();
    if ( scope != null) return scope.set( name, value);
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#set(java.lang.String, org.xmodel.IModelObject)
   */
  public List<IModelObject> set( String name, IModelObject value)
  {
    IVariableScope scope = getScope();
    if ( scope != null) return scope.set( name, value);
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#set(java.lang.String, java.util.List)
   */
  public List<IModelObject> set( String name, List<IModelObject> value)
  {
    IVariableScope scope = getScope();
    if ( scope != null) return scope.set( name, value);
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#set(java.lang.String, java.lang.Number)
   */
  public Number set( String name, Number value)
  {
    IVariableScope scope = getScope();
    if ( scope != null) return scope.set( name, value);
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#set(java.lang.String, java.lang.String)
   */
  public String set( String name, String value)
  {
    IVariableScope scope = getScope();
    if ( scope != null) return scope.set( name, value);
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#get(java.lang.String)
   */
  public Object get( String name)
  {
    return getParent().get( name);
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
    if ( parent != null) return parent.getExecutor();
    return GlobalSettings.getInstance().getDefaultExecutor();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#notifyBind(org.xmodel.xpath.expression.IExpression)
   */
  public void notifyBind( IExpression expression)
  {
    getParent().notifyBind( expression);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#notifyUnbind(org.xmodel.xpath.expression.IExpression)
   */
  public void notifyUnbind( IExpression expression)
  {
    getParent().notifyUnbind( expression);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#notifyUpdate(org.xmodel.xpath.expression.IExpression)
   */
  public void notifyUpdate( IExpression expression)
  {
    getParent().notifyUpdate( expression);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#markUpdate(org.xmodel.xpath.expression.IExpression)
   */
  public void markUpdate( IExpression expression)
  {
    getParent().markUpdate( expression);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#shouldUpdate(org.xmodel.xpath.expression.IExpression)
   */
  public boolean shouldUpdate( IExpression expression)
  {
    return getParent().shouldUpdate( expression);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#getLastUpdate(org.xmodel.xpath.expression.IExpression)
   */
  public Update getLastUpdate( IExpression expression)
  {
    return getParent().getLastUpdate( expression);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#getScope()
   */
  public IVariableScope getScope()
  {
    return getParent().getScope();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    int hashCode = object.hashCode();
    hashCode ^= position + (size << 16);
    hashCode ^= parent.hashCode();
    return hashCode;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals( Object arg0)
  {
    if ( arg0 == this) return true;
    if ( !(arg0 instanceof IContext)) return false;
    
    // compare this context
    IContext rhs = (IContext)arg0;
    if ( rhs.getObject() != getObject()) return false;
    if ( rhs.getPosition() != getPosition()) return false;
    if ( rhs.getSize() != getSize()) return false;
    
    // compare common parents
    IContext rhsParent = rhs.getParent();
    if ( parent == null || rhsParent == null) return true;
    if ( !parent.equals( rhsParent)) return false;
    
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#cloneOne()
   */
  public IContext cloneOne()
  {
    return new Context( objects);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#cloneTree()
   */
  public IContext cloneTree()
  {
    return new SubContext( parent, objects);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return super.toString();
  }

  private IContext parent;
  private List<Object> objects;
}
