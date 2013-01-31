/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * LocalContext.java
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

import org.xmodel.GlobalSettings;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * A StatefulContext which is introduced into the context chain to hold the variable assignments for
 * variable assignments which must be scoped by both the expression and the context.  This class 
 * does not start an update when the variable is changed.  This class is intended to be used within
 * the expression tree where updates will be the result of another atomic operation.
 */
public class LocalContext extends StatefulContext
{
  /**
   * Create a LocalContext with the specified parent which duplicates the parent context.
   * @param parent The parent context.
   */
  public LocalContext( IContext parent)
  {
    super( GlobalSettings.getInstance().getModel(), createScope( parent), parent.getObject(), parent.getPosition(), parent.getSize());
    this.parent = parent;
  }
  
  /**
   * Create a ContextScope which is parented with the ContextScope from the specified parent.
   * @param parent The parent context which may or may not have a ContextScope.
   * @return Returns the created ContextScope.
   */
  private static ContextScope createScope( IContext parent)
  {
    IVariableScope scope = parent.getScope();
    if ( scope != null) return new QuietScope( scope);
    return new QuietScope();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.StatefulContext#getParent()
   */
  @Override
  public IContext getParent()
  {
    return parent;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object object)
  {
    if ( object instanceof LocalContext)
      return parent.equals( ((LocalContext)object).getParent());
    return false;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    return parent.hashCode();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Context#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "local:");
    sb.append( super.toString());
    return sb.toString();
  }
  
  /**
   * A ContextScope which does not put an Update on the stack.
   */
  private static class QuietScope extends ContextScope
  {
    public QuietScope()
    {
      super();
    }

    public QuietScope( IVariableScope parent)
    {
      super( parent);
    }

    /* (non-Javadoc)
     * @see org.xmodel.xpath.variable.AbstractVariableScope#internal_set(java.lang.String, java.lang.Object)
     */
    @Override
    protected Object internal_set( String name, Object value)
    {
      Variable variable = getCreateVariable( name);
      if ( variable.value == null)
      {
        variable.value = value;
        return null;
      }
      else
      {
        Object oldValue = variable.value;
        variable.value = value;
        return oldValue;
      }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object object)
    {
      if ( object instanceof LocalContext)
      {
        return parent.equals( ((LocalContext)object).getParent());
      }
      return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
      return parent.hashCode();
    }
  }

  private IContext parent;
}