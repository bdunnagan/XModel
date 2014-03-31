package org.xmodel.xpath.function.statistics;

import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.function.Function;

/**
 * One-sided cumulative normal distribution function approximated by Taylor's series.
 */
public class CdfFunction extends Function
{
  public final static String name = "cdf";

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

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNumber(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    assertArgs( 2, 2);
    
    double x = getArgument( 0).evaluateNumber( context);
    double s = getArgument( 1).evaluateNumber( context);
    return cdf( x, s);  
  }
  
  /**
   * Cumulative normal distribution approximated by 100 terms.
   * @param x The value.
   * @param s The standard deviation of the distribution.
   * @return
   */
  private final static double cdf( double x, double s)
  {
    double c = Math.abs( x) / s;
    double cc = c * c;
    double sum = c;
    double a = c;
    
    for( int i=1; i<15; i++)
    {
      a *= cc;
      sum += a / f_odd[ i];
    }
    
    double y = (0.5 + sum / sqrt_pi2 * Math.exp( -cc / 2)) / s;
    return (x < 0)? 1 - y: y;
  }
  
//  /**
//   * Cumulative normal distribution approximated by 100 terms.
//   * @param x The value.
//   * @param s The standard deviation of the distribution.
//   * @return
//   */
//  private final static double cdfs( double x, double s)
//  {
//    double c = x / s;
//    double cc2 = 2 * c * c;
//    double sum = c;
//    double a = c;
//    for( int i=1; i<15; i++)
//    {
//      a *= cc2;
//      sum += a / f_n_odd[ i];
//    }
//
//    return 2 / sqrt_pi * Math.exp( -c * c) * sum;
//  }
//  
//  /**
//   * Cumulative normal distribution approximated by 100 terms.
//   * @param x The value.
//   * @param s The standard deviation of the distribution.
//   * @return
//   */
//  private final static double cdfh( double x, double s)
//  {
//    double c = x / s;
//    double e = 1 / c;
//    double ee = e * e;
//    double sum = e;
//    double a = e;
//    long b = 1;
//    
//    for( int i=0; i<14; )
//    {
//      a *= ee;
//      b <<= 1;
//      sum -= a * f_odd[ i++] / b;
//      
//      a *= ee;
//      b <<= 1;
//      sum += a * f_odd[ i++] / b;
//    }
//
//    return 1 - Math.exp( -c * c) / sqrt_pi * sum;
//  }
  
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
  
//  private final static double sqrt_pi = Math.sqrt( Math.PI);
  private final static double sqrt_pi2 = Math.sqrt( Math.PI * 2);
  
  private final static long[] f_odd = new long[] { 1, 3, 15, 105, 945, 10395, 135135, 2027025, 34459425, 654729075, 13749310575L, 316234143225L, 7905853580625L, 213458046676875L, 6190283353629375L};
//  private final static long[] f_n_odd = new long[] { 1, 3, 10, 42, 216, 1320, 9360, 75600, 685440, 6894720, 76204800, 918086400, 11975040000L, 168129561600L, 2528170444800L};
  
  public static void main( String[] args) throws Exception
  {
    for( double i=-6; i<=6; i+=0.1)
    {
      double a = CdfFunction.cdf( i, 1);
      System.out.printf( "%1.2f\t", i);
      System.out.println( a + "\t");
    }
  }
}
