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
import org.xmodel.net.Protocol;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * An XAction that executes a script identified by an expression.
 */
public class RunAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    var = Conventions.getVarName( document.getRoot(), false, "assign");    
    contextExpr = document.getExpression( "context", true);
    remoteExpr = document.getExpression( "remote", true);
    timeoutExpr = Xlate.get( document.getRoot(), "timeout", (IExpression)null);
    scriptExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override 
  @SuppressWarnings("unchecked")
  protected Object[] doAction( IContext context)
  {
    if ( remoteExpr == null) runLocal( context); else runRemote( context);   
    return null;
  }

  /**
   * Perform local execution.
   * @param context The context.
   * @return Returns the execution result.
   */
  @SuppressWarnings("unchecked")
  private Object[] runLocal( IContext context)
  {
    Object[] results = null;

    IXAction script = getScript( context);
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
    
    if ( var != null && results != null && results.length > 0)
    {
      Object result = results[ 0];
      IVariableScope scope = context.getScope();
      if ( result instanceof List) scope.set( var, (List<IModelObject>)result);
      else if ( result instanceof String) scope.set( var, result.toString());
      else if ( result instanceof Number) scope.set( var, (Number)result);
      else if ( result instanceof Boolean) scope.set( var, (Boolean)result);
    }
    
    return null;
  }
  
  /**
   * Perform remote execution.
   * @param context The context.
   * @return Returns the execution result.
   */
  private Object[] runRemote( IContext context)
  {
    Protocol protocol = getProtocol( context);
    //protocol.execute( )
    return null;
  }
  
  /**
   * Returns the instance of Protocol for remote invocation.
   * @param context The context.
   * @return Returns the instance of Protocol for remote invocation.
   */
  private Protocol getProtocol( IContext context)
  {
    IModelObject holder = remoteExpr.queryFirst( context);
    if ( holder == null) throw new XActionException( "Remote instance not found.");
    
    Object object = holder.getValue();
    if ( object == null) throw new XActionException( "Invalid remote instance.");
    
    if ( object instanceof StartClientAction)
    {
      return ((StartClientAction)object).getClient();
    }
    else if ( object instanceof StartServerAction)
    {
      return ((StartServerAction)object).getServer();
    }
    else
    {
      throw new XActionException( "Invalid remote instance.");
    }
  }
  
  /**
   * Get the script to be executed.
   * @param context The context.
   * @return Returns null or the script.
   */
  private IXAction getScript( IContext context)
  {
    IXAction script = null;
    if ( scriptExpr == null)
    {
      IModelObject scriptNode = document.getRoot();
      CompiledAttribute attribute = (scriptNode != null)? (CompiledAttribute)scriptNode.getAttribute( "compiled"): null;
      if ( attribute != null) script = attribute.script;
      if ( script == null)
      {
        script = document.createScript( scriptNode, "var", "context", "remote", "timeout");
        scriptNode.setAttribute( "compiled", new CompiledAttribute( script));
      }
    }
    else
    {
      IModelObject scriptNode = scriptExpr.queryFirst( context);
      CompiledAttribute attribute = (scriptNode != null)? (CompiledAttribute)scriptNode.getAttribute( "compiled"): null;
      if ( attribute != null) script = attribute.script;
      if ( script == null)
      {
        script = document.createScript( scriptNode);
        scriptNode.setAttribute( "compiled", new CompiledAttribute( script));
      }
    }
    return script;
  }
  
  private final static class CompiledAttribute
  {
    public CompiledAttribute( IXAction script)
    {
      this.script = script;
    }
    
    public IXAction script;
  }

  private String var;
  private IExpression contextExpr;
  private IExpression remoteExpr;
  private IExpression timeoutExpr;
  private IExpression scriptExpr;
}
