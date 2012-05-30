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

import java.io.IOException;
import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.log.SLog;
import org.xmodel.net.Server;
import org.xmodel.net.Session;
import org.xmodel.net.stream.Connection;
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
    scriptExpr = document.getExpression();
    
    varsExpr = document.getExpression( "vars", true);
    hostExpr = document.getExpression( "host", true);
    remoteExpr = document.getExpression( "remote", true);
    timeoutExpr = document.getExpression( "timeout", true);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override 
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
    String host = (hostExpr != null)? hostExpr.evaluateString( context): null;
    int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
    
    String vars = (varsExpr != null)? varsExpr.evaluateString( context): "";
    String[] varArray = vars.split( "\\s*,\\s*");
    
    // attempt to connect with cached session
    try
    {
      // create session on demand
      if ( session == null) session = getSession( context, host, timeout);

      // execute
      Object[] result = session.execute( (StatefulContext)context, varArray, getScriptNode( context), timeout);
      if ( var != null && result != null && result.length > 0) context.getScope().set( var, result[ 0]);
      return null;
    }
    catch( IOException e)
    {
      SLog.warnf( this, "Failed to execute script: %s", e.getMessage());
    }
    
    // attempt to create new session
    try
    {
      // create new session
      session = getSession( context, host, timeout);

      // execute
      Object[] result = session.execute( (StatefulContext)context, varArray, getScriptNode( context), timeout);
      if ( var != null && result != null && result.length > 0) context.getScope().set( var, result[ 0]);
      return null;
    }
    catch( IOException e)
    {
      throw new XActionException( e);
    }
  }

  /**
   * Get or create a session with the specified timeout.
   * @param context The context.
   * @param host The remote host (if remote is client).
   * @param timeout The timeout in milliseconds.
   * @return Returns the session.
   */
  private Session getSession( IContext context, String host, int timeout) throws IOException
  {
    IModelObject holder = remoteExpr.queryFirst( context);
    if ( holder == null) throw new XActionException( "Remote instance not found.");
    
    Object object = holder.getValue();
    if ( object == null) throw new XActionException( "Invalid remote instance.");
    
    if ( object instanceof Session)
    {
      return (Session)object;
    }
    else if ( object instanceof StartClientAction)
    {
      return ((StartClientAction)object).getClient().connect( timeout);
    }
    else if ( object instanceof StartServerAction)
    {
      Server server = ((StartServerAction)object).getServer();
      
      List<Connection> connections = server.getConnections( host);
      if ( connections.size() == 0)
      {
        throw new IOException( String.format( 
            "No client connections from host %s available for remote execution", host));
      }
      
      for( int i=0; i<connections.size(); i++)
      {
        Connection connection = connections.get( i);
        try
        {
          return server.openSession( connection);
        }
        catch( IOException e)
        {
          SLog.warnf( this, "Failed to create session from server over connection %d of %d to host %s: %s", 
              i, connections.size(), host, e.getMessage());
        }
      }
      
      throw new IOException( "Failed to create session to host: "+host);
    }
    else
    {
      throw new XActionException( "Invalid remote instance.");
    }
  }
  
  /**
   * Get the script node to be executed.
   * @param context The context.
   * @return Returns null or the script node.
   */
  private IModelObject getScriptNode( IContext context)
  {
    if ( scriptExpr != null) return scriptExpr.queryFirst( context);
    
    if ( inline == null)
    {
      inline = new ModelObject( "script");
      ModelAlgorithms.copyChildren( document.getRoot(), inline, null);
    }
    return inline;
  }
  
  /**
   * Get the script to be executed.
   * @param context The context.
   * @return Returns null or the script.
   */
  private IXAction getScript( IContext context)
  {
    IXAction script = null;
    if ( scriptExpr != null)
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
  private IExpression varsExpr;
  private IExpression contextExpr;
  private IExpression remoteExpr;
  private IExpression hostExpr;
  private IExpression timeoutExpr;
  private IExpression scriptExpr;
  private Session session;
  private IModelObject inline;
}
