  /*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * An XAction that provides improved semantics for invoking scripts.
 */
public class InvokeAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    variable = Xlate.get( document.getRoot(), "assign", "result");
    contextExpr = document.getExpression( "context", true);
    scriptExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @SuppressWarnings("unchecked")
  @Override
  protected Object[] doAction( IContext context)
  {
    Object[] results = null;

    CompiledAttribute attribute = (scriptNode != null)? (CompiledAttribute)scriptNode.getAttribute( "compiled"): null;
    ScriptAction script = (attribute != null)? attribute.script: null;
    
    IModelObject node = scriptExpr.queryFirst( context);
    if ( node != null && (script == null || scriptNode != node))
    {
      script = document.createScript( node);
      scriptNode = node;
      scriptNode.setAttribute( "compiled", new CompiledAttribute( script));
    }
        
    if ( script == null) return null;
    
    if ( contextExpr != null)
    {
      StatefulContext local = new StatefulContext( context, contextExpr.queryFirst( context));
      results = script.run( local);
    }
    else
    {
      results = script.run( context);
    }
    
    if ( results != null && results.length > 0)
    {
      Object result = results[ 0];
      IVariableScope scope = context.getScope();
      if ( result instanceof List) scope.set( variable, (List<IModelObject>)result);
      else if ( result instanceof String) scope.set( variable, result.toString());
      else if ( result instanceof Number) scope.set( variable, (Number)result);
      else if ( result instanceof Boolean) scope.set( variable, (Boolean)result);
    }
    
    return null;
  }
  
  private final static class CompiledAttribute
  {
    public CompiledAttribute( ScriptAction script)
    {
      this.script = script;
    }
    
    public ScriptAction script;
  }

  private String variable;
  private IExpression contextExpr;
  private IExpression scriptExpr;
  private IModelObject scriptNode;
}
