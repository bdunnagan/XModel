/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * CopyAction.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.xaction;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.AnnotatingChangeSet;
import org.xmodel.IChangeSet;
import org.xmodel.INode;
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

    INode root = document.getRoot();
    matcher = Conventions.getMatcher( document, root);
    differ.setMatcher( matcher);
    differ.setFactory( new ModelObjectFactory());
        
    // get expressions
    sourceExpr = document.getExpression( "source", true);
    targetExpr = document.getExpression( "target", true);
    ignoreExpr = document.getExpression( "ignore", true);
    orderedExpr = document.getExpression( "ordered", true);
    
    orderAll = Xlate.get( root, "orderAll", false);
    
    // get alternate source and target location
    if ( sourceExpr == null) sourceExpr = document.getExpression();
    if ( targetExpr == null) targetExpr = document.getExpression();
    
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
  protected Object[] doAction( IContext context)
  {
    INode source = sourceExpr.queryFirst( context);
    if ( source == null) return null;
    
    List<INode> ignoreList = new ArrayList<INode>();
    List<INode> orderedList = new ArrayList<INode>();
    
    List<INode> targets = targetExpr.query( context, null);
    for( INode target: targets) 
    {
      // if matcher class wasn't specified then use configurable matcher
      // TODO: need a more flexible configuration here
      if ( matcher instanceof ConfiguredXmlMatcher)
      {
        ConfiguredXmlMatcher matcher = (ConfiguredXmlMatcher)this.matcher;
        matcher.setOrderAll( orderAll);
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
    
    return null;
  }
  
  private XmlDiffer differ = new XmlDiffer();
  private IXmlMatcher matcher;
  private IExpression sourceExpr;
  private IExpression targetExpr;
  private IExpression ignoreExpr;
  private IExpression orderedExpr;
  private IChangeSet changeSet;
  private boolean orderAll;
}
