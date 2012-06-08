/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * StartClientAction.java
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

import java.io.IOException;
import java.util.concurrent.Executors;

import org.xmodel.IDispatcher;
import org.xmodel.IModelObject;
import org.xmodel.ThreadPoolDispatcher;
import org.xmodel.net.Client;
import org.xmodel.net.Server;
import org.xmodel.xaction.debug.Debugger;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * An XAction that creates a client endpoint that uses the XModel network protocol.
 * For more information about the protocol @see org.xmodel.net.Protocol Protocol.
 */
public class StartClientAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    hostExpr = document.getExpression( "host", true);
    portExpr = document.getExpression( "port", true);
    timeoutExpr = document.getExpression( "timeout", true);
    debugExpr = document.getExpression( "debug", true);
    daemonExpr = document.getExpression( "daemon", true);
    threadsExpr = document.getExpression( "threads", true);
    contextExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    // install debugger
    if ( (debugExpr != null)? debugExpr.evaluateBoolean( context): false)
    {
      XAction.setDebugger( new Debugger());
    }

    // start client
    try
    {
      String host = (hostExpr != null)? hostExpr.evaluateString( context): "127.0.0.1";
      int port = (portExpr != null)? (int)portExpr.evaluateNumber( context): Server.defaultPort;
      int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): 3000;
      boolean daemon = (daemonExpr != null)? daemonExpr.evaluateBoolean( context): true;
      int threads = (threadsExpr != null)? (int)threadsExpr.evaluateNumber( context): 0;
      
      IModelObject clientContextNode = (contextExpr != null)? contextExpr.queryFirst( context): null;
      IContext clientContext = (clientContextNode != null)? new StatefulContext( context.getScope(), clientContextNode): context;
      
      Client client = new Client( host, port, timeout, daemon);
      client.setServerContext( clientContext);
    }
    catch( IOException e)
    {
      throw new XActionException( e);
    }
    
    return null;
  }

  /**
   * Get client cached in the specified context variable.
   * @param context The context.
   * @param host The host.
   * @param port The port.
   * @return Returns null or the Client instance.
   */
  protected static Client getClient( IContext context, String host, int port)
  {
    String var = String.format( "%s:%d", host, port);
    Cached cached = (Cached)Conventions.getCache( context, var);
    return (cached != null)? cached.client: null;
  }
    
  /**
   * Disconnect the client (and all sessions).
   * @param host The host.
   * @param port The port.
   */
  protected static void stop( IContext context, String host, int port) throws IOException
  {
    String var = String.format( "%s:%d", host, port);
    Cached cached = (Cached)Conventions.getCache( context, var);
    if ( cached != null)
    {
      if ( cached.client != null) cached.client.disconnect();
      if ( cached.dispatcher != null) cached.dispatcher.shutdown( true);
      Conventions.putCache( context, var, null);
    }
  }
  
  protected static class Cached
  {
    public Client client;
    public IDispatcher dispatcher;
  }
  
  private IExpression hostExpr;
  private IExpression portExpr;
  private IExpression timeoutExpr;
  private IExpression contextExpr;
  private IExpression debugExpr;
  private IExpression daemonExpr;
  private IExpression threadsExpr;
}
