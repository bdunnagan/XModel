/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelAlgorithms;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * A GuardedAction which uses the ModelAlgorithms class to create a subtree defined by an IPath.
 * The path is given by the source expression which must begin with a location step.
 */
public class CreatePathAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#configure(org.xmodel.ui.model.ViewModel)
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
   * @see org.xmodel.ui.swt.form.actions.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    ModelAlgorithms.createPathSubtree( context, sourceExpr, factory, null);
  }
  
  private IModelObjectFactory factory;
  private IExpression sourceExpr;
}
