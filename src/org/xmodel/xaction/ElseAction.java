/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IfAction.java
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

import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * An action which executes its children if the preceding IfAction test is false.
 */
public class ElseAction extends XAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.XAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    elseScript = document.createScript( "true", "false", "test");
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#run(org.xmodel.xpath.expression.IContext)
   */
  public Object[] doRun( IContext context)
  {
    IVariableScope scope = context.getScope();
    Boolean result = (Boolean)scope.get( IfAction.testResultVariable);
    if ( !result) return elseScript.run( context);
    return null;
  }
  
  private IXAction elseScript;
}
