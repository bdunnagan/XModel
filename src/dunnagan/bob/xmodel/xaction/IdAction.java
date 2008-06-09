/*
 * Stonewall Networks, Inc.
 *
 *   Project: XModel
 *   Author:  bdunnagan
 *   Date:    Apr 27, 2008
 *
 * Copyright 2008.  Stonewall Networks, Inc.
 */
package dunnagan.bob.xmodel.xaction;

import java.util.Random;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.util.Radix;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;
import dunnagan.bob.xmodel.xpath.variable.IVariableScope;

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
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#configure(dunnagan.bob.xmodel.xaction.XActionDocument)
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
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
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
