/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * RunAction.java
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
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * An XAction which executes zero or more XActions identified by an XPath expression. Each element
 * returned by the expression is compiled into an XAction and executed.
 */
public class RunAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.GuardedAction#configure(
   * com.stonewall.cornerstone.cpmi.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    actionExpr = document.getExpression( document.getRoot());
  }

  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    StatefulContext docContext = new StatefulContext( context, document.getRoot());

    List<IXAction> actions = new ArrayList<IXAction>( 1);
    List<IModelObject> elements = actionExpr.query( docContext, null);
    for( IModelObject element: elements)
    {
      IXAction action = document.getAction( element);
      if ( action != null) actions.add( action);
    }
    
    for( IXAction action: actions)
    {
      Object[] result = action.run( context);
      if ( result != null) return result;
    }
    
    return null;
  }
  
  private IExpression actionExpr;
}
