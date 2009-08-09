/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.external.IExternalReference;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction which marks one or more external references dirty.
 */
public class MarkDirtyAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    targetExpr = document.getExpression( document.getRoot());    
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    for( IModelObject target: targetExpr.query( context, null))
    {
      target = ModelAlgorithms.dereference( target);
      if ( target instanceof IExternalReference)
        ((IExternalReference)target).clearCache();
    }
    
    return null;
  }

  private IExpression targetExpr;
}
