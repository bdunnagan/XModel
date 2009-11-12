/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * VariableSource.java
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
package org.xmodel.xpath.variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression.ResultType;


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
   * @see org.xmodel.xpath.variable.IVariableSource#setParent(org.xmodel.xpath.variable.IVariableSource)
   */
  public void setParent( IVariableSource parent)
  {
    this.parent = parent;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableSource#getParent()
   */
  public IVariableSource getParent()
  {
    return parent;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableSource#addScope(org.xmodel.xpath.variable.IVariableScope)
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
   * @see org.xmodel.xpath.variable.IVariableSource#removeScope(org.xmodel.xpath.variable.IVariableScope)
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
   * @see org.xmodel.xpath.variable.IVariableSource#getScopes()
   */
  public List<IVariableScope> getScopes()
  {
    return Collections.unmodifiableList( scopes);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableSource#getScope(java.lang.String)
   */
  public IVariableScope getScope( String scopeName)
  {
    for( IVariableScope scope: scopes)
      if ( scope.getName().equals( scopeName))
        return scope;
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.variable.IVariableSource#getVariableScope(java.lang.String)
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
   * @see org.xmodel.xpath.variable.IVariableSource#getVariableType(java.lang.String)
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
   * @see org.xmodel.xpath.variable.IVariableSource#getVariableType(java.lang.String, 
   * org.xmodel.xpath.expression.IContext)
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
   * @see org.xmodel.xpath.variable.IVariableSource#getVariable(java.lang.String, 
   * org.xmodel.xpath.expression.IContext)
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
    StringBuilder sb = new StringBuilder();
    
    // create set of all variables
    Set<String> set = new HashSet<String>();
    for( IVariableScope scope: getScopes())
      set.addAll( scope.getAll());
    
    List<String> names = new ArrayList<String>( set);
    Collections.sort( names);
    for( String name: names)
    {
      sb.append( name); sb.append( "=");
      IVariableScope scope = getVariableScope( name);
      sb.append( scope.get( name));
    }
    
    return sb.toString();
  }

  IVariableSource parent;
  List<IVariableScope> scopes;
}
