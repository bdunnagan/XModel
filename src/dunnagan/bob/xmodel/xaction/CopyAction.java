/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import java.util.ArrayList;
import java.util.List;

import dunnagan.bob.xmodel.*;
import dunnagan.bob.xmodel.diff.ConfiguredXmlMatcher;
import dunnagan.bob.xmodel.diff.IXmlMatcher;
import dunnagan.bob.xmodel.diff.RegularChangeSet;
import dunnagan.bob.xmodel.diff.XmlDiffer;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;
import dunnagan.bob.xmodel.xpath.expression.StatefulContext;

public class CopyAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.IXAction#configure(dunnagan.bob.xmodel.ui.model.ViewModel)
   */
  public void configure( XActionDocument document)
  {
    super.configure( document);

    IModelObject viewRoot = document.getRoot();
    IXmlMatcher matcher = getMatcher( viewRoot);
    differ.setMatcher( matcher);
    differ.setFactory( new ModelObjectFactory());
        
    // get expressions
    sourceExpr = document.getExpression( "source", false);
    targetExpr = document.getExpression( "target", false);
    ignoreExpr = document.getExpression( "ignore", false);
    orderedExpr = document.getExpression( "ordered", false);
    
    // get operation (with backward compatability)
    String opName = Xlate.get( document.getRoot(), "op", "copy");
    if ( opName.equals( "copy")) changeSet = new RegularChangeSet();
    else if ( opName.equals( "union")) changeSet = new UnionChangeSet();
    else if ( opName.equals( "intersect")) changeSet = new IntersectChangeSet();
    else if ( opName.equals( "annotate")) changeSet = new AnnotatingChangeSet();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.actions.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  protected void doAction( IContext context)
  {
    IModelObject source = sourceExpr.queryFirst( context);
    if ( source == null) return;
    
    List<IModelObject> ignoreList = new ArrayList<IModelObject>();
    List<IModelObject> orderedList = new ArrayList<IModelObject>();
    
    List<IModelObject> targets = targetExpr.query( context, null);
    for( IModelObject target: targets) 
    {
      // configure matcher
      ConfiguredXmlMatcher matcher = new ConfiguredXmlMatcher();
      differ.setMatcher( matcher);
      
      IContext sourceContext = new StatefulContext( context, source);
      IContext targetContext = new StatefulContext( context, target);
      if ( ignoreExpr != null)
      {
        ignoreList.clear();
        ignoreExpr.query( sourceContext, ignoreList);
        ignoreExpr.query( targetContext, ignoreList);
        matcher.ignore( ignoreList);
      }
      
      if ( orderedExpr != null)
      {
        orderedList.clear();
        orderedExpr.query( sourceContext, orderedList);
        orderedExpr.query( targetContext, orderedList);
        matcher.setOrdered( orderedList);
      }
      
      // diff
      changeSet.clearChanges();
      differ.diff( target, source, changeSet);
      changeSet.applyChanges();
    }
  }
  
  private XmlDiffer differ = new XmlDiffer();
  private IExpression sourceExpr;
  private IExpression targetExpr;
  private IExpression ignoreExpr;
  private IExpression orderedExpr;
  private IChangeSet changeSet;
}
