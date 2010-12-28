/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ContextAction.java
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

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * An XAction which executes one or more nested actions within an isolated nested context.
 * The nested context is a StatefulContext created with the input context as its parent.
 */
public class ContextAction extends XAction
{
  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.GuardedAction#configure(
   * com.stonewall.cornerstone.cpmi.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    sourceExpr = document.getExpression( "source", true);
    script = document.createScript( "source");
  }

  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.IXAction#run(org.xmodel.xpath.expression.IContext)
   */
  public Object[] doRun( IContext context)
  {
    if ( sourceExpr != null)
    {
      IModelObject source = sourceExpr.queryFirst( context);
      if ( source != null)
      {
        StatefulContext nested = new StatefulContext( context, source);
        return script.run( nested);
      }
    }
    else
    {
      StatefulContext nested = new StatefulContext( 
        context,
        context.getObject(),
        context.getPosition(),
        context.getSize());
      
      return script.run( nested);
    }
    
    return null;
  }

  private IExpression sourceExpr;
  private ScriptAction script;
}
