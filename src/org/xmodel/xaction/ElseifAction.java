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
import org.xmodel.xpath.expression.IExpression;

public class ElseifAction extends XAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    condition = document.getExpression( "true", true);
    if ( condition == null)
    {
      condition = document.getExpression( "test", true);
      if ( condition == null)
      {
        condition = document.getExpression( "false", true);
        negate = true;
      }
    }

    script = document.createScript( "true", "false", "test");
  }

  /**
   * Evaluate this condition and run script if condition is met.
   * @param context The context.
   * @return Returns the result of the evaluation.
   */
  public Result runTest( IContext context)
  {
    Result result = new Result();
    result.test = condition.evaluateBoolean( context);
    if ( negate ^ result.test) result.returned = script.run( context);
    return result;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#run(org.xmodel.xpath.expression.IContext)
   */
  public Object[] doRun( IContext context)
  {
    throw new IllegalStateException();
  }
  
  static class Result
  {
    public boolean test;
    public Object[] returned;
  }
  
  protected IExpression condition;
  protected ScriptAction script;
  protected boolean negate;
}
