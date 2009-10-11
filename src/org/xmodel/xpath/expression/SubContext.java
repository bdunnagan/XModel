/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xpath.expression;

import java.util.List;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
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
    this( parent, context.getObject(), context.getPosition(), context.getSize());
  }
  
  /**
   * Create a context for the given context node.
   * @param parent The parent of this context.
   * @param object The context node.
   */
  public SubContext( IContext parent, IModelObject object, int position, int size)
  {
    if ( parent == null) throw new IllegalArgumentException( "SubContext must have parent.");
    this.parent = parent;
    this.object = object;
    this.position = position;
    this.size = size;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#getModel()
   */
  public IModel getModel()
  {
    return (object != null)? object.getModel(): null;
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
  public int getLastUpdate( IExpression expression)
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
    return new Context( object, position, size);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IContext#cloneTree()
   */
  public IContext cloneTree()
  {
    return new SubContext( parent, object, position, size);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "sub:( "); sb.append( position);
    sb.append( ", "); sb.append( size);
    sb.append( ", "); sb.append( object);
    sb.append( ")");
    return sb.toString();
  }

  private IContext parent;
  private IModelObject object;
  private int position;
  private int size;
}
