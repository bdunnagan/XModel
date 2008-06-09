/*
 * Stonewall Networks, Inc.
 *
 *   Project: XModel
 *   Author:  bdunnagan
 *   Date:    Mar 13, 2008
 *
 * Copyright 2008.  Stonewall Networks, Inc.
 */
package dunnagan.bob.xmodel.xaction;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.external.IExternalReference;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * An XAction which calls the <code>flush</code> method of the ICachingPolicy associated
 * with each element identified by the source expression.
 */
public class FlushExternalAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#configure(dunnagan.bob.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    sourceExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    for( IModelObject source: sourceExpr.query( context, null))
    {
      if ( source instanceof IExternalReference)
      {
        try
        {
          ((IExternalReference)source).flush();
        }
        catch( Exception e)
        {
          System.err.println( "Unable to flush caching policy for reference: "+source);
          e.printStackTrace( System.err);
        }
      }
    }
  }

  private IExpression sourceExpr;
}
