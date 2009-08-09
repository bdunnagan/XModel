/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.Random;
import org.xmodel.IModelObject;
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
    IModelObject root = document.getRoot();
    minExpr = document.getExpression( "min", true);
    maxExpr = document.getExpression( "max", true);
    decimal = Xlate.get( document.getRoot(), "decimal", false);
    radix = Xlate.get( root, "radix", -1);
    variable = Xlate.get( document.getRoot(), "assign", (String)null); 
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
      if ( variable != null)
      {
        IVariableScope scope = context.getScope();
        scope.set( variable, value);
      }
      if ( targetExpr != null)
      {
        for( IModelObject target: targetExpr.query( context, null))
          target.setValue( value);
      }
    }
    else
    {
      if ( variable != null)
      {
        IVariableScope scope = context.getScope();
        scope.set( variable, number);
      }
      if ( targetExpr != null)
      {
        for( IModelObject target: targetExpr.query( context, null))
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
  private String variable;
  private IExpression targetExpr;
  private IExpression minExpr;
  private IExpression maxExpr;
}
