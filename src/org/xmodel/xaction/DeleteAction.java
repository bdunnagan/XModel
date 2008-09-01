/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


public class DeleteAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    targetExpr = document.getExpression( "target", false);
    if ( targetExpr == null) targetExpr = document.getExpression( document.getRoot());
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  protected void doAction( IContext context)
  {
    List<IModelObject> targets = targetExpr.evaluateNodes( context);
    for( IModelObject target: targets) target.removeFromParent();
  }
  
  IExpression targetExpr;
}
