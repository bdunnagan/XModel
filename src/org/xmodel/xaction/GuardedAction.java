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
 * An XAction which is only executed if its condition expression evaluates true. The condition
 * expression is defined in the condition element in the viewmodel. Implementations should 
 * override the <code>doAction</code> method to perform their implementation-specific behavior.
 * The condition expression is optional.
 */
public abstract class GuardedAction extends XAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    // get condition from 'when' element
    condition = document.getExpression( "when", false);
    
    // get condition from 'when' attribute
    if ( condition == null)
    {
      IModelObject attribute = document.getRoot().getAttributeNode( "when");
      if ( attribute != null) condition = document.getExpression( attribute);
    }
    
    // get condition from obsolete 'condition' element
    if ( condition == null) condition = document.getExpression( "condition", false);
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#run(org.xmodel.xpath.expression.IContext)
   */
  public final void doRun( IContext context)
  {
    if ( condition == null || condition.evaluateBoolean( context))
      doAction( context);
  }

  /**
   * Called if the condition evaluates true.
   * @param context The context.
   */
  abstract protected void doAction( IContext context);

  private IExpression condition;
}
