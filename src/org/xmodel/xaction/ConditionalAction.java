/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ConditionalAction.java
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

/**
 * An action which executes on of two actions depending on the result of a condition evaluation.
 * @deprecated Use IfAction instead.
 */
public class ConditionalAction extends XAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    ifExpr = document.getExpression( "if", false);
    thenAction = document.getAction( "then");
    elseAction = document.getAction( "else");
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#run(org.xmodel.xpath.expression.IContext)
   */
  public Object[] doRun( IContext context)
  {
    if ( ifExpr.evaluateBoolean( context))
    {
      return thenAction.run( context);
    }
    else if ( elseAction != null)
    {
      return elseAction.run( context);
    }
    
    return null;
  }
  
  IExpression ifExpr;
  IXAction thenAction;
  IXAction elseAction;
}
