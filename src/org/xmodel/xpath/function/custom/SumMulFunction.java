/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SumFunction.java
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
package org.xmodel.xpath.function.custom;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.function.Function;

/**
 * A custom XPath function, similar to the sum() function, that performs a sum of the results
 * of multiplying numbers.  The numbers that are multiplied may be pair-wise, or a single number
 * multiplied by a list of numbers, depending on whether a single number, or a list of numbers
 * is returned by one or both arguments.
 */
public class SumMulFunction extends Function
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "sum-mul";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NUMBER;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateNumber(
   * org.xmodel.xpath.expression.IContext)
   */
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    assertArgs( 2, 2);
    
    double sum = 0;
    
    IExpression arg0 = getArgument( 0);
    IExpression arg1 = getArgument( 1);
    
    switch( arg0.getType( context))
    {
      case NODES:  
      {
        switch( arg1.getType( context))
        {
          case NODES:  
          {
            List<IModelObject> nodes0 = arg0.evaluateNodes( context);
            List<IModelObject> nodes1 = arg1.evaluateNodes( context);
            for( int i=0; i<nodes0.size(); i++)
            {
              sum += Xlate.get( nodes0.get( i), 0d) * Xlate.get( nodes1.get( i), 0d);
            }
          }
          break;
          
          case NUMBER:
          case STRING:
          case BOOLEAN:
          {
            List<IModelObject> nodes0 = arg0.evaluateNodes( context);
            double value1 = arg1.evaluateNumber( context);
            for( int i=0; i<nodes0.size(); i++)
            {
              sum += Xlate.get( nodes0.get( i), 0d) * value1;
            }
          }
          break;
        }
      }
      break;
      
      case NUMBER:
      case STRING:
      case BOOLEAN:
      {
        double value0 = arg0.evaluateNumber( context);
        switch( arg1.getType( context))
        {
          case NODES:  
          {
            List<IModelObject> nodes1 = arg1.evaluateNodes( context);
            for( int i=0; i<nodes1.size(); i++)
            {
              sum += value0 * Xlate.get( nodes1.get( i), 0d);
            }
          }
          break;
          
          case NUMBER:
          case STRING:
          case BOOLEAN:
          {
            double value1 = arg1.evaluateNumber( context);
            sum += value0 * value1;
          }
          break;
        }
      }
      break;
    }
    
    return sum;
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
   * @see org.xmodel.xpath.expression.Expression#notifyValue(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext[], org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    if ( getParent() != null) notifyChange( this, contexts[ 0]);
  }
}
