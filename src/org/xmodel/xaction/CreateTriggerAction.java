/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * CreateTriggerAction.java
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
import org.xmodel.Xlate;
import org.xmodel.xaction.trigger.EntityTrigger;
import org.xmodel.xaction.trigger.ITrigger;
import org.xmodel.xaction.trigger.SourceTrigger;
import org.xmodel.xaction.trigger.WhenTrigger;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * An action which creates and activates a trigger. The trigger remains active
 * until it is deactived by a calling the <i>cancelTrigger</i> action.
 */
public class CreateTriggerAction extends XAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.XAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    IModelObject root = document.getRoot();
    
    // variable to hold trigger instance
    variable = Xlate.get( root, "assign", (String)null);
    
    // trigger
    String source = Xlate.get( root, "source", (String)null);
    if ( source != null) trigger = new SourceTrigger();
    
    String when = Xlate.get( root, "when", (String)null);
    if ( when != null) trigger = new WhenTrigger();
    
    String entity = Xlate.get( root, "entity", (String)null);
    if ( entity != null) trigger = new EntityTrigger();
    
    trigger.configure( document);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.XAction#doRun(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public Object[] doRun( IContext context)
  {
    IVariableScope scope = context.getScope();
    if ( scope != null) scope.setPojo( variable, trigger, null);
    trigger.activate( context);
    
    return null;
  }

  private String variable;
  private ITrigger trigger;
}
