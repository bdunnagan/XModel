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
import org.xmodel.BlockingDispatcher;
import org.xmodel.IDispatcher;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ThreadPoolDispatcher;
import org.xmodel.log.SLog;
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
    
    var = Conventions.getVarName( document.getRoot(), true);
    factory = Conventions.getFactory( document.getRoot());
    hostExpr = document.getExpression( "host", true);
    portExpr = document.getExpression( "port", true);
    timeoutExpr = document.getExpression( "timeout", true);
    daemonExpr = document.getExpression( "daemon", true);
    threadsExpr = document.getExpression( "threads", true);
    blockingExpr = document.getExpression( "blocking", true);
    contextExpr = document.getExpression( "context", true);
    if ( contextExpr == null) contextExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    // get context
    IModelObject serverContextNode = (contextExpr != null)? contextExpr.queryFirst( context): null;
    
    // start server
    try
    {
      String host = (hostExpr != null)? hostExpr.evaluateString( context): "0.0.0.0";
      int port = (portExpr != null)? (int)portExpr.evaluateNumber( context): Server.defaultPort;
      int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
      boolean daemon = (daemonExpr != null)? daemonExpr.evaluateBoolean( context): true;
      int threads = (threadsExpr != null)? (int)threadsExpr.evaluateNumber( context): 0;
      boolean blocking = (blockingExpr != null)? blockingExpr.evaluateBoolean( context): false;
      
      IContext serverContext = (serverContextNode != null)? new StatefulContext( context.getScope(), serverContextNode): context;
      if ( threads > 0) 
      {
        IDispatcher dispatcher = new ThreadPoolDispatcher( Executors.newFixedThreadPool( threads));
        serverContext.getModel().setDispatcher( dispatcher);
        Conventions.putCache( context, "org.xmodel.xaction.StartServerAction.dispatcher", dispatcher);
      }
      else if ( serverContext.getModel().getDispatcher() == null || blocking)
      {
        IDispatcher dispatcher = new BlockingDispatcher();
        serverContext.getModel().setDispatcher( dispatcher);
        Conventions.putCache( context, "org.xmodel.xaction.StartServerAction.dispatcher", dispatcher);
      }
      
      Server server = new Server( host, port, timeout);
      server.setServerContext( serverContext);
      server.start( daemon);
      Conventions.putCache( context, "org.xmodel.xaction.StartServerAction.server", server);
      
      StatefulContext stateful = (StatefulContext)context;
      IModelObject object = factory.createObject( null, "server");
      object.setValue( this);
      stateful.set( var, object);
    }
    catch( IOException e)
    {
      SLog.exception( this, e);
      Conventions.putCache( context, "org.xmodel.xaction.StartServerAction.server", null);
      throw new XActionException( e);
    }

    IDispatcher dispatcher = (IDispatcher)Conventions.getCache( context, "org.xmodel.xaction.StartServerAction.dispatcher");
    if ( dispatcher != null && dispatcher instanceof BlockingDispatcher)
    {
      BlockingDispatcher blocking = (BlockingDispatcher)dispatcher;
      while( true)
        if ( !blocking.process())
          break;
    }
    
    return null;
  }
  
  /**
   * @param context The context.
   * @return Returns the Server instance.
   */
  protected Server getServer( IContext context)
  {
    return (Server)Conventions.getCache( context, "org.xmodel.xaction.StartServerAction.server");
  }
  
  /**
   * Called by StopServerAction.
   * @param context The context.
   */
  protected void stop( IContext context)
  {
    Server server = getServer( context);
    if ( server != null)
    {
      server.stop();
      Conventions.putCache( context, "org.xmodel.xaction.StartServerAction.server", null);
    }
    
    IDispatcher dispatcher = (IDispatcher)Conventions.getCache( context, "org.xmodel.xaction.StartServerAction.dispatcher");
    if ( dispatcher != null)
    {
      dispatcher.shutdown( true);
      dispatcher = null;
    }
  }
  
  private String var;
  private IExpression hostExpr;
  private IExpression portExpr;
  private IExpression timeoutExpr;
  private IExpression contextExpr;
  private IExpression blockingExpr;
  private IExpression daemonExpr;
  private IExpression threadsExpr;
  private IModelObjectFactory factory;
}
