/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ReturnAction.java
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
import org.xmodel.xpath.expression.IExpression.ResultType;

/**
 * An xaction that returns from within a script executed with the InvokeAction. One or more
 * values can be returned per the contract of the invocation.
 */
public class ReturnAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    resultExpr = document.getExpression();
    if ( resultExpr == null) resultExpr = document.getExpression( "result", true);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    if ( resultExpr != null)
    {
      ResultType type = resultExpr.getType( context);
      switch( type)
      {
        case NODES: return new Object[] { resultExpr.evaluateNodes( context)};
        case NUMBER: return new Object[] { resultExpr.evaluateNumber( context)};
        case STRING: return new Object[] { resultExpr.evaluateString( context)};
        case BOOLEAN: return new Object[] { resultExpr.evaluateBoolean( context)};
        default: break;
      }
    }
    return new Object[ 0];
  }
  
  private IExpression resultExpr;
}
