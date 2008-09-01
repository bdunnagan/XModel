/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.HashMap;
import java.util.Map;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * An XAction which behaves like a select/case statement. *
 */
public class SelectAction extends XAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    // get select
    sourceExpr = document.getExpression( "source", true);

    // get cases
    caseScripts = new HashMap<String, ScriptAction>();
    for( IModelObject node: document.getRoot().getChildren( "case"))
    {
      ScriptAction script = document.createScript( node);
      caseScripts.put( node.getID(), script);
    }
    
    // default case
    IModelObject defaultElement = getDocument().getRoot().getFirstChild( "default");
    if ( defaultElement != null) defaultScript = document.createScript( defaultElement);
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#run(org.xmodel.xpath.expression.IContext)
   */
  public void doRun( IContext context)
  {
    String selection = sourceExpr.evaluateString( context);
    ScriptAction action = caseScripts.get( selection);
    if ( action != null) 
    {
      action.run( context);
    }
    else if ( defaultScript != null) 
    {
      defaultScript.run( context);
    }
  }

  private IExpression sourceExpr;
  private Map<String, ScriptAction> caseScripts;
  private ScriptAction defaultScript;
}
