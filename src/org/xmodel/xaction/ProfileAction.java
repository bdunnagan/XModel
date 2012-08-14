/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ProfileAction.java
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
import org.xmodel.xpath.variable.IVariableScope;

/**
 * An XAction which executes a script an stores the amount of time the script took to execute
 * in the variable defined by the <i>assign</i> attribute. Time is measured in nanoseconds.
 */
public class ProfileAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    IModelObject config = getDocument().getRoot();
    var = Conventions.getVarName( config, true, "assign");    
    
    // reuse ScriptAction to handle for script (must temporarily remove condition if present)
    Object when = config.removeAttribute( "when");
    script = document.createScript( "source");
    if ( when != null) config.setAttribute( "when", when);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    long t0 = System.nanoTime();
    
    Object[] result = script.run( context);
    
    long t1 = System.nanoTime();
    double elapsed = (t1 - t0);
    
    // store elapsed time in variable
    IVariableScope scope = null;
    if ( scope == null)
    {
      scope = context.getScope();
      if ( scope == null)
        throw new IllegalArgumentException( 
          "ProfileAction context does not have a variable scope: "+this);
    }
    
    scope.set( var, elapsed);
    
    return result;
  }

  private String var;
  private ScriptAction script;
}
