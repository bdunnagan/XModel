/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * StartServerAction.java
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
import org.xmodel.net.Server;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * An XAction that creates a server endpoint that uses the XModel network protocol.  
 * For more information about the protocol @see org.xmodel.net.Protocol Protocol.
 * There are three different server threading models.  If the <i>threads</i> parameter
 * is supplied, then the server will create a ThreadPoolDispatcher and assign it to the
 * server context. Otherwise, if the calling thread does not have a dispatcher, or if the
 * <i>blocking</i> parameter is true, then the server will create a BlockingDispatcher, 
 * assign it to the server context, and the action will not terminate unless the dispatcher 
 * is shutdown.
 */
public class StartServerAction extends GuardedAction
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
    daemonExpr = document.getExpression( "daemon", true);
    threadsExpr = document.getExpression( "threads", true);
    contextExpr = document.getExpression( "context", true);
    if ( contextExpr == null) contextExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    // start server
    try
    {
      String host = (hostExpr != null)? hostExpr.evaluateString( context): "0.0.0.0";
      int port = (portExpr != null)? (int)portExpr.evaluateNumber( context): Server.defaultPort;
      int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
      boolean daemon = (daemonExpr != null)? daemonExpr.evaluateBoolean( context): true;
      int threads = (threadsExpr != null)? (int)threadsExpr.evaluateNumber( context): 0;
      
      IModelObject serverContextNode = (contextExpr != null)? contextExpr.queryFirst( context): null;
      IContext serverContext = (serverContextNode != null)? new StatefulContext( context.getScope(), serverContextNode): context;
      
      String var = String.format( "%s:%d", host, port);
      Cached cached = (Cached)Conventions.getCache( context, var);
      if ( cached != null)
      {
        if ( cached.server.getHost().equals( host) && cached.server.getPort() == port)
          return null;
      }
      
      Server server = new Server( host, port);
      server.setPingTimeout( timeout);
      server.setServerContext( serverContext);
      server.start( daemon);
      
      cached = new Cached();
      cached.server = server;
      Conventions.putCache( context, var, cached);
      
      if ( threads > 0)
      {
        server.setDispatcher( new ThreadPoolDispatcher( Executors.newFixedThreadPool( threads)));
        cached.dispatcher = server.getDispatcher();
      }
      else
      {
        server.setDispatcher( context.getModel().getDispatcher());
      }
    }
    catch( IOException e)
    {
      throw new XActionException( e);
    }

    return null;
  }

  /**
   * Get server cached in the specified context variable.
   * @param context The context.
   * @param var The context variable.
   * @return Returns null or the Server instance.
   */
  protected static Server getServer( IContext context, String var)
  {
    Cached cached = (Cached)Conventions.getCache( context, var);
    return (cached != null)? cached.server: null;
  }
    
  /**
   * Disconnect the server in the specified context variable.
   * @param context The context.
   * @param var The context variable.
   */
  protected static void stop( IContext context, String var) throws IOException
  {
    Cached cached = (Cached)Conventions.getCache( context, var);
    if ( cached != null)
    {
      if ( cached.server != null) cached.server.stop();
      if ( cached.dispatcher != null) cached.dispatcher.shutdown( true);
      Conventions.putCache( context, var, null);
    }
  }
  
  protected static class Cached
  {
    public Server server;
    public IDispatcher dispatcher;
  }
  
  private IExpression hostExpr;
  private IExpression portExpr;
  private IExpression timeoutExpr;
  private IExpression contextExpr;
  private IExpression daemonExpr;
  private IExpression threadsExpr;
}
