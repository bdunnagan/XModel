/*
 * Stonewall Networks, Inc.
 *
 *   Project: XModel
 *   Author:  bdunnagan
 *   Date:    Apr 16, 2008
 *
 * Copyright 2008.  Stonewall Networks, Inc.
 */
package dunnagan.bob.xmodel.xaction;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.net.ModelServer;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * An XAction which creates and starts a ModelServer.
 */
public class StopServerAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#configure(dunnagan.bob.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    serverExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
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
