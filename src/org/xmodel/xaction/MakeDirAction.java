/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import java.io.File;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that creates one or more directories.
 */
public class MakeDirAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    recurseExpr = document.getExpression( "recurse", true);
    pathExpr = document.getExpression( "path", true);
    if ( pathExpr == null) pathExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    File file = new File( pathExpr.evaluateString( context));
    if ( !file.exists()) 
    {
      if ( recurseExpr == null || !recurseExpr.evaluateBoolean( context))
      {
        file.mkdir();
      }
      else
      {
        file.mkdirs();
      }
    }
    
    return null;
  }

  private IExpression pathExpr;
  private IExpression recurseExpr;
}
