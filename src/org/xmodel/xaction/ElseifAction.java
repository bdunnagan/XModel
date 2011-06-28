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

/**
 * An action which executes its children if the preceding IfAction or ElseIfAction test is false.
 */
public class ElseifAction extends IfAction
{
  /**
   * Set the IfAction with which this ElseAction pairs.
   * @param action The IfAction.
   */
  public void setIf( IfAction action)
  {
    if ( !(action instanceof IfAction)) throw new XActionException( "An 'else' element does not following an 'if' element.");
    ifScript = (IfAction)action;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#run(org.xmodel.xpath.expression.IContext)
   */
  public Object[] doRun( IContext context)
  {
    if ( !ifScript.test)
    {
      test = condition.evaluateBoolean( context);
      if ( negate ^ test) return script.run( context);
    }
    else
    {
      test = true;
    }
    return null;
  }
  
  private IfAction ifScript;
}
