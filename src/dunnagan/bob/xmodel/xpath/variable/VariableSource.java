/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dunnagan.bob.xmodel.xpath.expression.ExpressionException;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression.ResultType;

/**
 * A reference implementation of IVariableSource.
 */
public class VariableSource implements IVariableSource
{
  public VariableSource()
  {
    scopes = new ArrayList<IVariableScope>( 1);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.variable.IVariableSource#setParent(dunnagan.bob.xmodel.xpath.variable.IVariableSource)
   */
  public void setParent( IVariableSource parent)
  {
    this.parent = parent;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.variable.IVariableSource#getParent()
   */
  public IVariableSource getParent()
  {
    return parent;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.variable.IVariableSource#addScope(dunnagan.bob.xmodel.xpath.variable.IVariableScope)
   */
  public void addScope( IVariableScope scope)
  {
    if ( scope == null) return;
    
    for( int i=0; i<scopes.size(); i++)
    {
      IVariableScope current = scopes.get( i);
      if ( current == scope) return;
      if ( current.getPrecedence() > scope.getPrecedence())
      {
        scope.internal_setSource( this);
        scopes.add( i, scope);
        return;
      }
    }

    scope.internal_setSource( this);
    scopes.add( scope);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.variable.IVariableSource#removeScope(dunnagan.bob.xmodel.xpath.variable.IVariableScope)
   */
  public void removeScope( IVariableScope scope)
  {
    if ( scope == null) return;
    
    for( int i=scopes.size()-1; i>=0; i--)
    {
      IVariableScope current = scopes.get( i);
      if ( current == scope)
      {
        scope.internal_setSource( null);
        scopes.remove( i);
        return;
      }
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.variable.IVariableSource#getScopes()
   */
  public List<IVariableScope> getScopes()
  {
    return Collections.unmodifiableList( scopes);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.variable.IVariableSource#getScope(java.lang.String)
   */
  public IVariableScope getScope( String scopeName)
  {
    for( IVariableScope scope: scopes)
      if ( scope.getName().equals( scopeName))
        return scope;
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.variable.IVariableSource#getVariableScope(java.lang.String)
   */
  public IVariableScope getVariableScope( String variable)
  {
    // resolve in this scope
    for( int i=scopes.size()-1; i>=0; i--)
    {
      IVariableScope scope = scopes.get( i);
      if ( scope.isDefined( variable)) 
        return scope;
    }
    
    // resolve in parent
    if ( parent != null) return parent.getVariableScope( variable);
    
    // unresolved
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.variable.IVariableSource#getVariableType(java.lang.String)
   */
  public ResultType getVariableType( String variable)
  {
    // resolve in this scope
    for( int i=scopes.size()-1; i>=0; i--)
    {
      IVariableScope scope = scopes.get( i);
      if ( scope.isDefined( variable))
        return scope.getType( variable);
    }
    
    // resolve in parent
    if ( parent != null) return parent.getVariableType( variable);
    
    // unresolved
    return ResultType.UNDEFINED;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.variable.IVariableSource#getVariableType(java.lang.String, 
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public ResultType getVariableType( String variable, IContext context)
  {
    // resolve in this scope
    for( int i=scopes.size()-1; i>=0; i--)
    {
      IVariableScope scope = scopes.get( i);
      if ( scope.isDefined( variable))
        return scope.getType( variable, context);
    }
    
    // resolve in parent
    if ( parent != null) return parent.getVariableType( variable, context);
    
    // unresolved
    return ResultType.UNDEFINED;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.variable.IVariableSource#getVariable(java.lang.String, 
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public Object getVariable( String variable, IContext context) throws ExpressionException
  {
    // resolve in this source
    IVariableScope scope = getVariableScope( variable);
    Object result = scope.get( variable, context);
    if ( result != null) return result;
    
    // resolve in parent
    if ( parent != null) return parent.getVariable( variable, context);
    
    // unresolved
    return null;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder string = new StringBuilder();
    
    // print parent first
    IVariableSource parent = getParent();
    if ( parent != null) 
    {
      string.append( "Source: "); string.append( hashCode()); string.append( "\n");
      string.append( parent.toString());
      string.append( "\n");
   }
    
    // print this
    for( IVariableScope scope: getScopes())
    {
      string.append( "\nScope: "); string.append( scope.getName()); string.append( "\n");
      string.append( scope.toString());
    }
    
    return string.toString();
  }

  IVariableSource parent;
  List<IVariableScope> scopes;
}
