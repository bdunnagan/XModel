/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

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
    whileExpr = document.getExpression( "while", false);
    script = document.createScript( actionExpr);;
  }

  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    while( whileExpr.evaluateBoolean( context))
      script.run( context);
  }
  
  private final static IExpression actionExpr = XPath.createExpression(
    "*[ not( matches( name(), '^while|when|condition$'))]");

  private IExpression whileExpr;
  private ScriptAction script;
}
