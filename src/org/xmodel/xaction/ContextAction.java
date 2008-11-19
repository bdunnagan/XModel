/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * An XAction which executes one or more nested actions within an isolated nested context.
 * The nested context is a StatefulContext created with the input context as its parent.
 */
public class ContextAction extends XAction
{
  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.GuardedAction#configure(
   * com.stonewall.cornerstone.cpmi.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    sourceExpr = document.getExpression( "source", false);
    script = document.createScript( "source");
  }

  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.IXAction#run(org.xmodel.xpath.expression.IContext)
   */
  public void doRun( IContext context)
  {
    if ( sourceExpr != null)
    {
      IModelObject source = sourceExpr.queryFirst( context);
      if ( source != null)
      {
        StatefulContext nested = new StatefulContext( context, source);
        script.run( nested);
      }
    }
    else
    {
      StatefulContext nested = new StatefulContext( 
        context,
        context.getObject(),
        context.getPosition(),
        context.getSize());
      
      script.run( nested);
    }
  }

  private IExpression sourceExpr;
  private ScriptAction script;
}
