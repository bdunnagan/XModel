/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.Random;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.util.Radix;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.variable.IVariableScope;


/**
 * An XAction which generates a random identifier with default length of 10.
 */
public class IdAction extends GuardedAction
{
  public IdAction()
  {
    random = new Random( System.nanoTime());
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    variable = Xlate.get( document.getRoot(), "assign", (String)null);
    targetExpr = Xlate.get( document.getRoot(), (IExpression)null);
    length = Xlate.get( document.getRoot(), "length", 10);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    if ( targetExpr != null)
    {
      for ( IModelObject node: targetExpr.query( context, null))
        node.setValue( generate( length));
    }
    else if ( variable != null)
    {
      IVariableScope scope = context.getScope();
      if ( scope != null) scope.set( variable, generate( length));
    }
    else
    {
      context.getObject().setValue( generate( length));
    }
    
    return null;
  }
  
  /**
   * Generate an ID of the specified length.
   * @param length The length.
   * @return Returns the ID.
   */
  private String generate( int length)
  {
    StringBuilder sb = new StringBuilder();
    for( int i=0; i<length; i=sb.length())
    {
      long value = random.nextLong();
      sb.append( Radix.convert( value, 36).toUpperCase());
    }
    sb.setLength( length);
    return sb.toString();
  }
  
  private Random random;
  private String variable;
  private IExpression targetExpr;
  private int length;
  
  public static void main( String[] args)
  {
    Random random = new Random();
    int length = 3;
    StringBuilder sb = new StringBuilder();
    for( int k=0; k<1000; k++)
    {
      sb.setLength( 0);
      for( int i=0; i<length; i=sb.length())
      {
        long value = random.nextLong();
        sb.append( Radix.convert( value, 36).toUpperCase());
      }
      sb.setLength( length);
      System.out.println( sb);
    }
  }
}
