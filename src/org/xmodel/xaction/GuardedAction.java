/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * GuardedAction.java
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
import org.xmodel.log.SLog;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction which is only executed if its condition expression evaluates true. The condition
 * expression is defined in the condition element in the viewmodel. Implementations should 
 * override the <code>doAction</code> method to perform their implementation-specific behavior.
 * The condition expression is optional.
 */
public abstract class GuardedAction extends XAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    // get condition from 'when' element
    condition = document.getExpression( "when", false);
    
    // get condition from 'when' attribute
    if ( condition == null)
    {
      IModelObject attribute = document.getRoot().getAttributeNode( "when");
      if ( attribute != null) condition = document.getExpression( attribute);
    }
    
    // get condition from obsolete 'condition' element
    if ( condition == null) condition = document.getExpression( "condition", false);
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#run(org.xmodel.xpath.expression.IContext)
   */
  public final Object[] doRun( IContext context)
  {
    if ( condition != null)
    {
      boolean result = condition.evaluateBoolean( context);
      SLog.debugf( this, "(%s) returned (%s)", condition, result);
      if ( !result) return null;
    }
    return doAction( context);
  }

  /**
   * Called if the condition evaluates true.
   * @param context The context.
   * @return Returns null or the return value (see IXAction).
   */
  abstract protected Object[] doAction( IContext context);

  private IExpression condition;
}
