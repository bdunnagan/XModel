/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * An action which executes on of two actions depending on the result of a condition evaluation.
 * @deprecated Use IfAction instead.
 */
public class ConditionalAction extends XAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.actions.XAction#configure(dunnagan.bob.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    ifExpr = document.getExpression( "if", false);
    thenAction = document.getAction( "then");
    elseAction = document.getAction( "else");
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.IXAction#run(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public void doRun( IContext context)
  {
    if ( ifExpr.evaluateBoolean( context))
    {
      thenAction.run( context);
    }
    else if ( elseAction != null)
    {
      elseAction.run( context);
    }
  }
  
  IExpression ifExpr;
  IXAction thenAction;
  IXAction elseAction;
}
