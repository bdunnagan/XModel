/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;
import dunnagan.bob.xmodel.xpath.expression.StatefulContext;

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
    script = document.createScript( actionExpr);;
  }

  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.IXAction#run(dunnagan.bob.xmodel.xpath.expression.IContext)
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

  private final static IExpression actionExpr = XPath.createExpression(
    "*[ not( matches( name(), '^when|condition|source$'))]");
  
  private IExpression sourceExpr;
  private ScriptAction script;
}
