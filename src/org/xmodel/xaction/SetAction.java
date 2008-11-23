/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelAlgorithms;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpression.ResultType;


/**
 * An IXAction which sets the value of the nodes defined by the target expression to the string
 * result of the source expression.
 */
public class SetAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#configure(org.xmodel.ui.model.ViewModel)
   */
  public void configure( XActionDocument document)
  {
    super.configure( document);
    sourceExpr = document.getExpression( "source", true);
    targetExpr = document.getExpression( "target", true);
    
    // alternate source or target location
    if ( sourceExpr == null) sourceExpr = document.getExpression();
    if ( targetExpr == null) targetExpr = document.getExpression();
    
    factory = getFactory( document.getRoot());
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  protected void doAction( IContext context)
  {
    List<IModelObject> targets = targetExpr.query( context, null);
    if ( targets.size() == 0) ModelAlgorithms.createPathSubtree( context, targetExpr, factory, null);

    // handle java.lang.Object transfer correctly
    Object value = null;
    if ( sourceExpr.getType( context) == ResultType.NODES)
    {
      IModelObject node = sourceExpr.queryFirst( context);
      value = node.getValue();
    }
    else
    {
      value = sourceExpr.evaluateString( context);
    }
    
    targets = targetExpr.query( context, null);
    for( IModelObject target: targets) target.setValue( value);
  }
  
  private IModelObjectFactory factory;
  private IExpression sourceExpr;
  private IExpression targetExpr;
}
