/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IModelObjectFactory;
import dunnagan.bob.xmodel.ModelAlgorithms;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * An IXAction which sets the value of the nodes defined by the target expression to the string
 * result of the source expression.
 */
public class SetAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.IXAction#configure(dunnagan.bob.xmodel.ui.model.ViewModel)
   */
  public void configure( XActionDocument document)
  {
    super.configure( document);
    sourceExpr = document.getExpression( "source", false);
    targetExpr = document.getExpression( "target", false);
    factory = getFactory( document.getRoot());
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.actions.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  protected void doAction( IContext context)
  {
    List<IModelObject> targets = targetExpr.query( context, null);
    if ( targets.size() == 0) ModelAlgorithms.createPathSubtree( context, targetExpr, factory, null);
    
    String value = sourceExpr.evaluateString( context);
    if ( value != null)
    {
      targets = targetExpr.query( context, null);
      for( IModelObject target: targets) target.setValue( value);
    }
  }
  
  private IModelObjectFactory factory;
  private IExpression sourceExpr;
  private IExpression targetExpr;
}
