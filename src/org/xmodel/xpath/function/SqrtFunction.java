/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * CountFunction.java
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
package org.xmodel.xpath.function;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * An implementation of the X-Path sqrt() function.
 */
public class SqrtFunction extends Function
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "sqrt";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NUMBER;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNumber(
   * org.xmodel.xpath.expression.IContext)
   */
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    return Math.sqrt( getArgument( 0).evaluateNumber( context));
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( getParent() != null) notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( getParent() != null) notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, double, double)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    if ( getParent() != null) notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyValue(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext[], org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    if ( getParent() != null) notifyChange( this, contexts[ 0]);
  }
}