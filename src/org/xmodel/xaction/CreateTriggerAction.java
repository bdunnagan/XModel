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

import java.util.List;
import org.xmodel.INode;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.log.SLog;
import org.xmodel.xaction.trigger.EntityTrigger;
import org.xmodel.xaction.trigger.ITrigger;
import org.xmodel.xaction.trigger.SourceTrigger;
import org.xmodel.xaction.trigger.WhenTrigger;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * An action which creates and activates a trigger. The trigger remains active
 * until it is deactivated by a calling the <i>cancelTrigger</i> action.
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
    
    INode config = document.getRoot();
    
    // variable to hold trigger instance
    var = Conventions.getVarName( config, true, "assign");
    
    // trigger
    String source = Xlate.get( config, "source", (String)null);
    if ( source != null) trigger = new SourceTrigger();
    
    String when = Xlate.get( config, "when", (String)null);
    if ( when != null) trigger = new WhenTrigger();
    
    String entity = Xlate.get( config, "entity", (String)null);
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
    if ( scope != null)
    {
      // cancel previous trigger in var
      Object object = scope.get( var);
      if ( object != null && object instanceof List<?>)
      {
        List<?> nodes = (List<?>)object;
        if ( nodes.size() > 0)
        {
          INode holder = (INode)nodes.get( 0);
          Object trigger = holder.getValue();
          if ( trigger instanceof ITrigger)
          {
            ((ITrigger)trigger).deactivate( context);
            holder.setValue( null);
          }
        }
      }
      
      // save trigger in var
      INode holder = new ModelObject( "trigger");
      holder.setValue( trigger);
      scope.set( var, holder);
    }
    
    SLog.debug( this, trigger);
    
    trigger.activate( context);
    return null;
  }

  private String var;
  private ITrigger trigger;
}
