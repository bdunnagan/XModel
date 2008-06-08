/*
 * Stonewall Networks, Inc.
 *
 *   Project: XMView
 *   Author:  bdunnagan
 *   Date:    Oct 8, 2007
 *
 * Copyright 2007.  Stonewall Networks, Inc.
 */
package dunnagan.bob.xmodel.xaction;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.external.IExternalReference;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * An XAction which marks one or more external references dirty.
 */
public class MarkDirtyAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#configure(dunnagan.bob.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    targetExpr = document.getExpression( document.getRoot());    
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    for( IModelObject target: targetExpr.query( context, null))
      if ( target instanceof IExternalReference)
        ((IExternalReference)target).clearCache();
  }

  private IExpression targetExpr;
}
