/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction which provides while-loop semantics.
 */
public class LoopAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.GuardedAction#configure(com.stonewall.cornerstone.cpmi.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    whileExpr = document.getExpression( "while", true);
    countExpr = document.getExpression( "count", true);
    script = document.createScript( "while", "count");
  }

  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    if ( countExpr == null)
    {
      while( whileExpr == null || whileExpr.evaluateBoolean( context))
      {
        Object[] result = script.run( context);
        if ( result != null) return result;
      }
    }
    else if ( whileExpr == null)
    {
      int count = (int)countExpr.evaluateNumber( context);
      for( int i=0; i<count; i++)
      {
        Object[] result = script.run( context);
        if ( result != null) return result;
      }
    }
    else
    {
      int count = (int)countExpr.evaluateNumber( context);
      for( int i=0; i<count && whileExpr.evaluateBoolean( context); i++)
      {
        Object[] result = script.run( context);
        if ( result != null) return result;
      }
    }
    
    return null;
  }
  
  private IExpression whileExpr;
  private IExpression countExpr;
  private ScriptAction script;
}
