/*
 * Stonewall Networks, Inc.
 *
 *   Project: XMView
 *   Author:  bdunnagan
 *   Date:    Oct 8, 2007
 *
 * Copyright 2007.  Stonewall Networks, Inc.
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
  protected void doAction( IContext context)
  {
    for( IModelObject target: targetExpr.query( context, null))
    {
      target = ModelAlgorithms.dereference( target);
      if ( target instanceof IExternalReference)
        ((IExternalReference)target).clearCache();
    }
  }

  private IExpression targetExpr;
}
