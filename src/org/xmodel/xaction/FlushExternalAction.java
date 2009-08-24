/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import org.xmodel.IModelObject;
import org.xmodel.external.IExternalReference;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction which calls the <code>flush</code> method of the ICachingPolicy associated
 * with each element identified by the source expression.
 * @deprecated Use FlushAction instead.
 */
public class FlushExternalAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    sourceExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
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
    
    return null;
  }

  private IExpression sourceExpr;
}
