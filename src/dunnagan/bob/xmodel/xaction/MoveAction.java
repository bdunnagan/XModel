/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * An XAction which reparents one or more elements to a single new parent.
 * Use DeleteAction to remove one or more elements from their parents.
 */
public class MoveAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.IXAction#configure(dunnagan.bob.xmodel.ui.model.ViewModel)
   */
  public void configure( XActionDocument document)
  {
    super.configure( document);

    if ( document.getRoot().getNumberOfChildren() == 0)
    {
      sourceExpr = document.getExpression( document.getRoot());
      targetExpr = defaultTargetExpr;
    }
    else
    {
      IModelObject viewRoot = document.getRoot();
      
      // get source and target expressions
      sourceExpr = document.getExpression( "source", false);
      targetExpr = document.getExpression( "target", false);
      indexExpr = document.getExpression( "index", false);
      
      // get flags (with backwards compatible support)
      Object attribute = viewRoot.getAttribute( "unique");
      unique = (attribute != null)? 
        Xlate.get( viewRoot, "unique", false): 
        Xlate.childGet( viewRoot, "unique", false);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.actions.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
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

  private final static IExpression defaultTargetExpr = XPath.createExpression( ".");
  
  private IExpression sourceExpr;
  private IExpression targetExpr;
  private IExpression indexExpr;
  private boolean unique;
}
