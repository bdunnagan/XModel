/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * MoveAction.java
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

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelAlgorithms;
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
    
    factory = getFactory( viewRoot);
    
    // get source and target expressions
    sourceExpr = document.getExpression( "source", true);
    targetExpr = document.getExpression( "target", true);
    indexExpr = document.getExpression( "index", true);
    
    // alternate form with either source or target expression defined in value
    if ( sourceExpr == null) sourceExpr = document.getExpression();
    if ( targetExpr == null) targetExpr = document.getExpression();
    
    // get flags
    create = Xlate.get( viewRoot, "create", Xlate.childGet( viewRoot, "create", false));
    unique = Xlate.get( viewRoot, "unique", Xlate.childGet( viewRoot, "unique", false));
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  protected Object[] doAction( IContext context)
  {
    // create target if requested
    if ( create) ModelAlgorithms.createPathSubtree( context, targetExpr, factory, null);
    
    // source
    List<IModelObject> sources = sourceExpr.query( context, null);
    if ( sources.size() == 0) return null;
    
    // fail if target is null
    List<IModelObject> targets = targetExpr.query( context, null);
    if ( targets.size() == 0) return null; 
    
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
    
    return null;
  }

  private IExpression sourceExpr;
  private IExpression targetExpr;
  private IExpression indexExpr;
  private IModelObjectFactory factory;
  private boolean create;
  private boolean unique;
}
