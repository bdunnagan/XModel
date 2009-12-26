/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SelectAction.java
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

import java.util.HashMap;
import java.util.Map;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * An XAction which behaves like a select/case statement. *
 */
public class SelectAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    // get select
    sourceExpr = document.getExpression( "source", true);

    // get cases
    caseScripts = new HashMap<String, ScriptAction>();
    for( IModelObject node: document.getRoot().getChildren( "case"))
    {
      ScriptAction script = document.createScript( node);
      caseScripts.put( node.getID(), script);
    }
    
    // default case
    IModelObject defaultElement = getDocument().getRoot().getFirstChild( "default");
    if ( defaultElement != null) defaultScript = document.createScript( defaultElement);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    String selection = sourceExpr.evaluateString( context);
    ScriptAction action = caseScripts.get( selection);
    if ( action != null) 
    {
      return action.run( context);
    }
    else if ( defaultScript != null) 
    {
      return defaultScript.run( context);
    }
    
    return null;
  }

  private IExpression sourceExpr;
  private Map<String, ScriptAction> caseScripts;
  private ScriptAction defaultScript;
}
