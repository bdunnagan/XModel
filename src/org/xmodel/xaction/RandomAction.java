/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * RandomAction.java
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
package org.xmodel.xaction;

import java.util.Random;
import org.xmodel.INode;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * An XAction which generates a random floating point number.
 */
public class RandomAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    INode config = document.getRoot();
    minExpr = document.getExpression( "min", true);
    maxExpr = document.getExpression( "max", true);
    decimal = Xlate.get( document.getRoot(), "decimal", false);
    radix = Xlate.get( config, "radix", -1);
    var = Conventions.getVarName( config, false, "assign");    
    targetExpr = document.getExpression();
    random = new Random();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    double min = 0;
    double max = 0;
    if ( minExpr != null) min = minExpr.evaluateNumber( context);
    if ( maxExpr != null) max = maxExpr.evaluateNumber( context);
    
    Number number = random( min, max, decimal);
    if ( radix != -1)
    {
      String value = convert( number, radix);
      if ( var != null)
      {
        IVariableScope scope = context.getScope();
        scope.set( var, value);
      }
      if ( targetExpr != null)
      {
        for( INode target: targetExpr.query( context, null))
          target.setValue( value);
      }
    }
    else
    {
      if ( var != null)
      {
        IVariableScope scope = context.getScope();
        scope.set( var, number);
      }
      if ( targetExpr != null)
      {
        for( INode target: targetExpr.query( context, null))
          target.setValue( number);
      }
    }
    
    return null;
  }

  /**
   * Generate a random number between the min and max values.
   * @param min The min value.
   * @param max The max value.
   * @param decimal True if a decimal should be generated.
   * @return Returns the new random number.
   */
  private Number random( double min, double max, boolean decimal)
  {
    double range = max - min;
    if ( decimal)
    {
      double value = random.nextDouble();
      if ( range == 0) return value;
      return value * range + min;
    }
    else
    {
      if ( range == 0) return random.nextLong();
      double value = random.nextDouble();
      return (long)(value * range + min);
    }
  }
  
  /**
   * Convert the random number to a string with the specified radix.
   * @param value The number.
   * @param radix The radix.
   * @return Returns the string.
   */
  private String convert( Number value, int radix)
  {
    if ( radix == -1) return null;
    if ( value instanceof Long)
    {
      return Long.toString( (Long)value, radix);
    }
    else if ( radix == 16)
    {
      return Double.toHexString( (Double)value);
    }
    else
    {
      return Double.toString( (Double)value);
    }
  }
  
  private Random random;
  private boolean decimal;
  private int radix;
  private String var;
  private IExpression targetExpr;
  private IExpression minExpr;
  private IExpression maxExpr;
}
