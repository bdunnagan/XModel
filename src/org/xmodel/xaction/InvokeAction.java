/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * InvokeAction.java
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

    variable = Xlate.get( document.getRoot(), "assign", (String)null);
    contextExpr = document.getExpression( "context", true);
    scriptExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override 
  @SuppressWarnings("unchecked")
  protected Object[] doAction( IContext context)
  {
    Object[] results = null;

    IXAction script = null;
    IModelObject node = scriptExpr.queryFirst( context);
    if ( node != null)
    {
      CompiledAttribute attribute = (node != null)? (CompiledAttribute)node.getAttribute( "compiled"): null;
      if ( attribute != null) script = attribute.script;
      if ( script == null)
      {
        script = document.createScript( node);
        node.setAttribute( "compiled", new CompiledAttribute( script));
      }
    }
        
    if ( script == null) return null;
    
    if ( contextExpr != null)
    {
      for( IModelObject localNode: contextExpr.query( context, null))
      {
        StatefulContext local = new StatefulContext( context, localNode);
        results = script.run( local);
      }
    }
    else
    {
      results = script.run( context);
    }
    
    if ( variable != null && results != null && results.length > 0)
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
    public CompiledAttribute( IXAction script)
    {
      this.script = script;
    }
    
    public IXAction script;
  }

  private String variable;
  private IExpression contextExpr;
  private IExpression scriptExpr;
}
