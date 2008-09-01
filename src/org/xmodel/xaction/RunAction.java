/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;


/**
 * An XAction which executes zero or more XActions identified by an XPath expression. Each element
 * returned by the expression is compiled into an XAction and executed. This XAction will return
 * false and stop executing on the first XAction that fails.
 */
public class RunAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.GuardedAction#configure(
   * com.stonewall.cornerstone.cpmi.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    actionExpr = document.getExpression( document.getRoot());
  }

  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    StatefulContext docContext = new StatefulContext( context, document.getRoot());
    
    List<IModelObject> elements = actionExpr.query( docContext, null);
    for( IModelObject element: elements)
    {
      IXAction action = document.getAction( element);
      if ( action != null) action.run( context);
    }
  }
  
  private IExpression actionExpr;
}
