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

import java.util.List;

import org.xmodel.INode;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction which behaves like a switch/case statement.
 */
public class SwitchAction extends GuardedAction
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
    List<INode> caseNodes = document.getRoot().getChildren( "case");
    caseExprs = new IExpression[ caseNodes.size()];
    caseScripts = new ScriptAction[ caseNodes.size()];
    for( int i=0; i<caseNodes.size(); i++)
    {
      INode caseNode = caseNodes.get( i);
      caseExprs[ i] = Xlate.get( caseNode, "value", (IExpression)null);
      caseScripts[ i] = document.createScript( caseNode);
    }
    
    // default case
    INode defaultElement = getDocument().getRoot().getFirstChild( "default");
    if ( defaultElement != null) defaultScript = document.createScript( defaultElement);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    String selection = sourceExpr.evaluateString( context);
    for( int i=0; i<caseExprs.length; i++)
    {
      String value = caseExprs[ i].evaluateString( context);
      if ( selection.equals( value))
      {
        return caseScripts[ i].run( context);
      }
    }
    
    if ( defaultScript != null) return defaultScript.run( context);
    
    return null;
  }

  private IExpression sourceExpr;
  private IExpression[] caseExprs;
  private ScriptAction[] caseScripts;
  private ScriptAction defaultScript;
}
