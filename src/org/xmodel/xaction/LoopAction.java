/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * LoopAction.java
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
 * An XAction which provides while-loop semantics.
 */
public class LoopAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.GuardedAction#configure(com.stonewall.cornerstone.cpmi.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    whileExpr = document.getExpression( "while", true);
    countExpr = document.getExpression( "count", true);
    script = document.createScript( "while", "count");
  }

  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    if ( countExpr == null)
    {
      while( whileExpr == null || whileExpr.evaluateBoolean( context))
      {
        Object[] result = script.run( context);
        if ( result != null) return result;
      }
    }
    else if ( whileExpr == null)
    {
      int count = (int)countExpr.evaluateNumber( context);
      for( int i=0; i<count; i++)
      {
        Object[] result = script.run( context);
        if ( result != null) return result;
      }
    }
    else
    {
      int count = (int)countExpr.evaluateNumber( context);
      for( int i=0; i<count && whileExpr.evaluateBoolean( context); i++)
      {
        Object[] result = script.run( context);
        if ( result != null) return result;
      }
    }
    
    return null;
  }
  
  private IExpression whileExpr;
  private IExpression countExpr;
  private ScriptAction script;
}
