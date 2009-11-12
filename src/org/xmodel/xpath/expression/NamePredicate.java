/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * NamePredicate.java
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
import java.util.Map;
import org.xmodel.IModelListener;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelListener;


/**
 * An implementation of IExpression which evaluates boolean and returns true if the context object has the
 * name with which the NamePredicate was constructed.
 */
public class NamePredicate extends PredicateExpression
{
  public NamePredicate( IPath path, String objectName)
  {
    super( path);
    this.objectName = objectName;
    this.contexts = new HashMap<IModelObject, IContext>();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "name-predicate";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.BOOLEAN;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateBoolean(
   * org.xmodel.xpath.expression.IContext)
   */
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    return context.getObject().getID().equals( objectName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#bind(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void bind( IContext context)
  {
    context.getObject().addModelListener( nameListener);
    contexts.put( context.getObject(), context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#unbind(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void unbind( IContext context)
  {
    context.getObject().removeModelListener( nameListener);
    contexts.remove( context.getObject());
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#cloneOne()
   */
  @Override
  protected IExpression cloneOne()
  {
    return new NamePredicate( path, objectName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.PredicateExpression#toString()
   */
  @Override
  public String toString()
  {
    return "[@id='"+objectName+"']";
  }

  final IModelListener nameListener = new ModelListener() {
    public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
    {
      if ( listeners == null) return;
      if ( attrName.equals( "id"))
      {
        if ( oldValue != null && oldValue.equals( objectName)) 
        {
          IExpressionListener[] array = listeners.toArray( new IExpressionListener[ 0]);
          for( IExpressionListener listener: array) 
            listener.notifyChange( NamePredicate.this, contexts.get( object), false);
        }
        if ( newValue != null && newValue.equals( objectName))
        {
          IExpressionListener[] array = listeners.toArray( new IExpressionListener[ 0]);
          for( IExpressionListener listener: array) 
            listener.notifyChange( NamePredicate.this, contexts.get( object), true);
        }
      }
    }
    public void notifyClear( IModelObject object, String attrName, Object oldValue)
    {
      if ( listeners == null) return;
      if ( attrName.equals( "id"))
      {
        if ( oldValue != null && oldValue.equals( objectName)) 
        {
          IExpressionListener[] array = listeners.toArray( new IExpressionListener[ 0]);
          for( IExpressionListener listener: array) 
            listener.notifyChange( NamePredicate.this, contexts.get( object), false);
        }
      }
    }
  };
  
  String objectName;
  Map<IModelObject, IContext> contexts;
}
