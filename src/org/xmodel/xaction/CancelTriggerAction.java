/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import org.xmodel.IModelObject;
import org.xmodel.xaction.trigger.ITrigger;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction which cancels a previously created trigger. The trigger must have
 * been successfully assigned to its instance variable. The trigger must be 
 * cancelled within the same context in which it was created.
 */
public class CancelTriggerAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    instanceExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    for( IModelObject holder: instanceExpr.query( context, null))
    {
      ITrigger trigger = (ITrigger)holder.getValue();
      if ( trigger != null) trigger.deactivate( context);
    }
    
    return null;
  }

  private IExpression instanceExpr;
}
