/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ExtentExpressionListener.java
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

import java.util.Collections;
import java.util.List;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IExpression.ResultType;


/**
 * An ExpressionListener which provides the complete new and old node-sets each time the value
 * of a node-set expression is updated. All of the other notification mechanisms are the same
 * as ExpressionListener.
 */
public abstract class ExtentExpressionListener extends ExpressionListener
{
  /**
   * Called whenever a node-set expression is updated.
   * @param expression The expression.
   * @param context The context.
   * @param newSet The complete new node-set.
   * @param oldSet The complete old node-set.
   */
  public abstract void notifyChange( IExpression expression, IContext context, List<IModelObject> newSet, List<IModelObject> oldSet);
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    IModel model = context.getModel();
    List<IModelObject> oldSet = Collections.emptyList();
    try
    {
      model.revert();
      oldSet = expression.evaluateNodes( context);
    }
    catch( ExpressionException e)
    {
      handleException( expression, context, e);
    }
    finally
    {
      model.restore();
    }

    List<IModelObject> newSet = Collections.emptyList();
    try
    {
      newSet = expression.evaluateNodes( context);
    }
    catch( ExpressionException e)
    {
      handleException( expression, context, e);
    }
    
    // notify
    notifyChange( expression, context, newSet, oldSet);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    IModel model = context.getModel();
    List<IModelObject> oldSet = Collections.emptyList();
    try
    {
      model.revert();
      oldSet = expression.evaluateNodes( context);
    }
    catch( ExpressionException e)
    {
      handleException( expression, context, e);
    }
    finally
    {
      model.restore();
    }

    List<IModelObject> newSet = Collections.emptyList();
    try
    {
      newSet = expression.evaluateNodes( context);
    }
    catch( ExpressionException e)
    {
      handleException( expression, context, e);
    }
    
    // notify
    notifyChange( expression, context, newSet, oldSet);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context)
  {
    if ( expression.getType( context) == ResultType.NODES)
    {
      IModel model = context.getModel();
      List<IModelObject> oldSet = Collections.emptyList();
      try
      {
        model.revert();
        oldSet = expression.evaluateNodes( context);
      }
      catch( ExpressionException e)
      {
        handleException( expression, context, e);
      }
      finally
      {
        model.restore();
      }
  
      List<IModelObject> newSet = Collections.emptyList();
      try
      {
        newSet = expression.evaluateNodes( context);
      }
      catch( ExpressionException e)
      {
        handleException( expression, context, e);
      }
      
      // notify
      notifyChange( expression, context, newSet, oldSet);
    }
  }
}
