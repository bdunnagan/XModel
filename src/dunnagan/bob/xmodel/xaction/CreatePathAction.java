/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import dunnagan.bob.xmodel.IModelObjectFactory;
import dunnagan.bob.xmodel.ModelAlgorithms;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * A GuardedAction which uses the ModelAlgorithms class to create a subtree defined by an IPath.
 * The path is given by the source expression which must begin with a location step.
 */
public class CreatePathAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.IXAction#configure(dunnagan.bob.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    sourceExpr = document.getExpression( "source", false);
    if ( sourceExpr == null) sourceExpr = document.getExpression( document.getRoot());
    factory = getFactory( document.getRoot());
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.actions.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    ModelAlgorithms.createPathSubtree( context, sourceExpr, factory, null);
  }
  
  private IModelObjectFactory factory;
  private IExpression sourceExpr;
}
