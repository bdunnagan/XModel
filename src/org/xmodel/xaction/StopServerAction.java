/*
 * Stonewall Networks, Inc.
 *
 *   Project: XModel
 *   Author:  bdunnagan
 *   Date:    Apr 16, 2008
 *
 * Copyright 2008.  Stonewall Networks, Inc.
 */
package org.xmodel.xaction;

import org.xmodel.IModelObject;
import org.xmodel.net.ModelServer;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction which creates and starts a ModelServer.
 */
public class StopServerAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    serverExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    IModelObject object = serverExpr.queryFirst( context);
    ModelServer server = (ModelServer)object.getValue();
    server.stop();
  }

  private IExpression serverExpr;
}
