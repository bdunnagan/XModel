/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.AnnotatingChangeSet;
import org.xmodel.IChangeSet;
import org.xmodel.IModelObject;
import org.xmodel.IntersectChangeSet;
import org.xmodel.ModelObjectFactory;
import org.xmodel.UnionChangeSet;
import org.xmodel.Xlate;
import org.xmodel.diff.ConfiguredXmlMatcher;
import org.xmodel.diff.IXmlMatcher;
import org.xmodel.diff.RegularChangeSet;
import org.xmodel.diff.XmlDiffer;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;


public class CopyAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#configure(org.xmodel.ui.model.ViewModel)
   */
  public void configure( XActionDocument document)
  {
    super.configure( document);

    IModelObject viewRoot = document.getRoot();
    matcher = getMatcher( viewRoot);
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
   * @see org.xmodel.ui.swt.form.actions.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
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
      // if matcher class wasn't specified then use configurable matcher
      // TODO: need a more flexible configuration here
      if ( matcher == null)
      {
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
      }
      
      // diff
      changeSet.clearChanges();
      differ.diff( target, source, changeSet);
      changeSet.applyChanges();
    }
  }
  
  private XmlDiffer differ = new XmlDiffer();
  private IXmlMatcher matcher;
  private IExpression sourceExpr;
  private IExpression targetExpr;
  private IExpression ignoreExpr;
  private IExpression orderedExpr;
  private IChangeSet changeSet;
}
