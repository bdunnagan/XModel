package org.xmodel.xpath.function.custom;

import java.util.List;
import java.util.Random;

import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.function.Function;

public class RandomFunction extends Function
{
  public final static String name = "random";
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  @Override
  public String getName()
  {
    return name;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  @Override
  public ResultType getType()
  {
    return ResultType.NUMBER;
  }

  @Override
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    double min = 0;
    double max = 1;
    boolean gaussian = false;
    
    List<IExpression> args = getArguments();
    
    if ( args.size() > 0) min = getArgument( 0).evaluateNumber( context);
    if ( args.size() > 1) max = getArgument( 1).evaluateNumber( context);
    if ( args.size() > 2) gaussian = getArgument( 2).evaluateBoolean( context);
    
    Random random = new Random();
    double r = gaussian? random.nextGaussian(): random.nextDouble();
    return r * (max - min) + min;
  }
}
