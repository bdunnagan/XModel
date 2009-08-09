/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpression.ResultType;

/**
 * An xaction that returns from within a script executed with the InvokeAction. One or more
 * values can be returned per the contract of the invocation.
 */
public class ReturnAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    resultExpr = document.getExpression();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    ResultType type = resultExpr.getType( context);
    switch( type)
    {
      case NODES: return new Object[] { resultExpr.evaluateNodes( context)};
      case NUMBER: return new Object[] { resultExpr.evaluateNumber( context)};
      case STRING: return new Object[] { resultExpr.evaluateString( context)};
      case BOOLEAN: return new Object[] { resultExpr.evaluateBoolean( context)};
    }
    return new Object[ 0];
  }
  
  private IExpression resultExpr;
}
