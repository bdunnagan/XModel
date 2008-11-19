/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
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
  public void doRun( IContext context)
  {
    IVariableScope scope = context.getScope();
    if ( scope != null) scope.setPojo( variable, trigger, null);
    trigger.activate( context);
  }

  private String variable;
  private ITrigger trigger;
}
