/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xpath.expression;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xmodel.IModelObject;
import org.xmodel.memento.IMemento;
import org.xmodel.memento.VariableMemento;
import org.xmodel.xpath.expression.IExpression.ResultType;
import org.xmodel.xpath.variable.AbstractVariableScope;
import org.xmodel.xpath.variable.IVariableListener;
import org.xmodel.xpath.variable.IVariableScope;
import org.xmodel.xpath.variable.Precedences;


/**
 * An implementation of IVariableScope for contexts which supports nested scopes so that nested
 * contexts may inherit or override the variable definitions of parent contexts.
 */
public class ContextScope extends AbstractVariableScope
{
  /**
   * Create a top-level ContextScope.
   */
  public ContextScope()
  {
  }
  
  /**
   * Create a ContextScope with a parent scope.
   * @param parent The parent scope.
   */
  public ContextScope( IVariableScope parent)
  {
    this.parent = parent;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#getPrecedence()
   */
  public int getPrecedence()
  {
    return Precedences.contextScope;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#getName()
   */
  public String getName()
  {
    return "context";
  }

  /**
   * Returns the parent scope.
   * @return Returns null or the parent scope.
   */
  public IVariableScope getParent()
  {
    return parent;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#get(java.lang.String, 
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public Object get( String name, IContext context) throws ExpressionException
  {
    Object result = super.get( name, context);
    if ( result != null) return result;
    if ( parent != null) return parent.get( name, context);
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#get(java.lang.String)
   */
  @Override
  public Object get( String name)
  {
    Object result = super.get( name);
    if ( result != null) return result;
    if ( parent != null) return parent.get( name);
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#getAll()
   */
  @Override
  public Collection<String> getAll()
  {
    if ( parent != null)
    {
      Set<String> names = new HashSet<String>();
      names.addAll( parent.getAll());
      names.addAll( super.getAll());
      return names;
    }
    return super.getAll();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#getType(java.lang.String)
   */
  @Override
  public ResultType getType( String name)
  {
    ResultType type = super.getType( name);
    if ( type != ResultType.UNDEFINED) return type;
    if ( parent != null) return parent.getType( name);
    return ResultType.UNDEFINED;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#getType(java.lang.String, 
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public ResultType getType( String name, IContext context)
  {
    ResultType type = super.getType( name, context);
    if ( type != ResultType.UNDEFINED) return type;
    if ( parent != null) return parent.getType( name, context);
    return ResultType.UNDEFINED;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#isDefined(java.lang.String)
   */
  @Override
  public boolean isDefined( String name)
  {
    if ( parent != null && parent.isDefined( name)) return true;
    return super.isDefined( name);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#isBound(java.lang.String)
   */
  @Override
  public boolean isBound( String name)
  {
    if ( super.isBound( name)) return true;
    if ( parent != null) return parent.isBound( name);
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#addListener(java.lang.String, 
   * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.variable.IVariableListener)
   */
  @Override
  public void addListener( String name, IContext context, IVariableListener listener)
  {
    if ( super.isDefined( name))
      super.addListener( name, context, listener);
    else if ( parent != null)
      parent.addListener( name, context, listener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#removeListener(java.lang.String, 
   * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.variable.IVariableListener)
   */
  @Override
  public void removeListener( String name, IContext context, IVariableListener listener)
  {
    if ( super.isDefined( name))
      super.removeListener( name, context, listener);
    else if ( parent != null)
      parent.removeListener( name, context, listener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#restore(org.xmodel.memento.IMemento)
   */
  @Override
  public void restore( IMemento iMemento)
  {
    reverted = false;
    VariableMemento memento = (VariableMemento)iMemento;
    if ( isDefined( memento.varName))
      super.restore( iMemento);
    else
      parent.restore( iMemento);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#revert(org.xmodel.memento.IMemento)
   */
  @Override
  public void revert( IMemento iMemento)
  {
    VariableMemento memento = (VariableMemento)iMemento;
    if ( isDefined( memento.varName))
      super.revert( iMemento);
    else
      parent.revert( iMemento);
    reverted = true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#define(java.lang.String, 
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public void define( String name, IExpression expression)
  {
    if ( reverted) throw new IllegalStateException( "Illegal set on reverted context: "+this);
    super.define( name, expression);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#set(java.lang.String, java.lang.Boolean)
   */
  @Override
  public Boolean set( String name, Boolean value)
  {
    if ( reverted) throw new IllegalStateException( "Illegal set on reverted context: "+this);
    return super.set( name, value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#set(java.lang.String, org.xmodel.IModelObject)
   */
  @Override
  public List<IModelObject> set( String name, IModelObject value)
  {
    if ( reverted) throw new IllegalStateException( "Illegal set on reverted context: "+this);
    return super.set( name, value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#set(java.lang.String, java.util.List)
   */
  @Override
  public List<IModelObject> set( String name, List<IModelObject> value)
  {
    if ( reverted) throw new IllegalStateException( "Illegal set on reverted context: "+this);
    return super.set( name, value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#set(java.lang.String, java.lang.Number)
   */
  @Override
  public Number set( String name, Number value)
  {
    if ( reverted) throw new IllegalStateException( "Illegal set on reverted context: "+this);
    return super.set( name, value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.AbstractVariableScope#set(java.lang.String, java.lang.String)
   */
  @Override
  public String set( String name, String value)
  {
    if ( reverted) throw new IllegalStateException( "Illegal set on reverted context: "+this);
    return super.set( name, value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableScope#cloneOne()
   */
  public IVariableScope cloneOne()
  {
    ContextScope clone = new ContextScope();
    if ( variables != null) clone.copyFrom( this);
    return clone;
  }
  
  IVariableScope parent;
  boolean reverted;
}
