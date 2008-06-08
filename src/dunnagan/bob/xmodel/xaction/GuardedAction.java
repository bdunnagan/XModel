/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * An XAction which is only executed if its condition expression evaluates true. The condition
 * expression is defined in the condition element in the viewmodel. Implementations should 
 * override the <code>doAction</code> method to perform their implementation-specific behavior.
 * The condition expression is optional.
 */
public abstract class GuardedAction extends XAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.IXAction#configure(dunnagan.bob.xmodel.ui.model.ViewModel)
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
   * @see dunnagan.bob.xmodel.ui.swt.form.IXAction#run(dunnagan.bob.xmodel.xpath.expression.IContext)
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
