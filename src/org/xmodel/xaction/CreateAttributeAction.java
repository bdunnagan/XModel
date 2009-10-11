/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that creates an attribute on an element.
 */
public class CreateAttributeAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    nameExpr = document.getExpression( "name", true);
    valueExpr = document.getExpression( "value", true);
    targetExpr = document.getExpression( "target", true);
    if ( targetExpr == null) targetExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    String name = nameExpr.evaluateString( context);
    String value = valueExpr.evaluateString( context);
    for( IModelObject target: targetExpr.query( context, null))
      target.setAttribute( name, value);
    
    return null;
  }

  private IExpression nameExpr;
  private IExpression valueExpr;
  private IExpression targetExpr;
}
