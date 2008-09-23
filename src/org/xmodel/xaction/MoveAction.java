/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * An XAction which reparents one or more elements to a single new parent.
 * Use DeleteAction to remove one or more elements from their parents.
 */
public class MoveAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#configure(org.xmodel.ui.model.ViewModel)
   */
  public void configure( XActionDocument document)
  {
    super.configure( document);

    IModelObject viewRoot = document.getRoot();
    
    // get source and target expressions
    sourceExpr = document.getExpression( "source", true);
    targetExpr = document.getExpression( "target", true);
    indexExpr = document.getExpression( "index", true);
    
    // alternate form with either source or target expression defined in value
    if ( sourceExpr == null) sourceExpr = document.getExpression();
    if ( targetExpr == null) targetExpr = document.getExpression();
    
    // get flags
    unique = Xlate.get( viewRoot, "unique", Xlate.childGet( viewRoot, "unique", false));
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  protected void doAction( IContext context)
  {
    // source
    List<IModelObject> sources = sourceExpr.query( context, null);
    if ( sources.size() == 0) return;
    
    // fail if target is null
    List<IModelObject> targets = targetExpr.query( context, null);
    if ( targets.size() == 0) return; 
    
    // index
    int index = -1;
    if ( indexExpr != null) index = (int)indexExpr.evaluateNumber( context);

    // move
    for( IModelObject target: targets)
    {
      int start = (index < 0)? target.getNumberOfChildren(): index;
      for( IModelObject source: sources)
      {
        if ( !unique || target.getChild( source.getType(), source.getID()) == null)
          target.addChild( source, start);
        start++;
      }
    }
  }

  private IExpression sourceExpr;
  private IExpression targetExpr;
  private IExpression indexExpr;
  private boolean unique;
}
