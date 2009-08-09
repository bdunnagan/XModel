/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An action which executes on of two actions depending on the result of a condition evaluation.
 */
public class IfAction extends XAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    condition = document.getExpression( "true", true);
    if ( condition == null)
    {
      condition = document.getExpression( "false", true);
      negate = true;
    }
    
    thenScript = document.createChildScript( "then");
    elseScript = document.createChildScript( "else");
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#run(org.xmodel.xpath.expression.IContext)
   */
  public Object[] doRun( IContext context)
  {
    if ( negate ^ condition.evaluateBoolean( context)) return thenScript.run( context); 
    else return elseScript.run( context);
  }

  private IExpression condition;
  private ScriptAction thenScript;
  private ScriptAction elseScript;
  private boolean negate;
}
