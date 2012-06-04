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
import org.xmodel.IModelObjectFactory;
import org.xmodel.ThreadPoolDispatcher;
import org.xmodel.log.SLog;
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
    
    var = Conventions.getVarName( document.getRoot(), true);
    factory = Conventions.getFactory( document.getRoot());
    hostExpr = document.getExpression( "host", true);
    portExpr = document.getExpression( "port", true);
    timeoutExpr = document.getExpression( "timeout", true);
    retriesExpr = document.getExpression( "retries", true);
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
    // get context
    IModelObject clientContextNode = (contextExpr != null)? contextExpr.queryFirst( context): null;
    
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
      int retries = (retriesExpr != null)? (int)retriesExpr.evaluateNumber( context): Integer.MAX_VALUE;
      boolean daemon = (daemonExpr != null)? daemonExpr.evaluateBoolean( context): true;
      int threads = (threadsExpr != null)? (int)threadsExpr.evaluateNumber( context): 0;
      
      IContext clientContext = (clientContextNode != null)? new StatefulContext( context.getScope(), clientContextNode): context;
      
      IDispatcher dispatcher = clientContext.getModel().getDispatcher();
      if ( dispatcher == null && threads == 0) threads = 1;
      
      if ( threads > 0) 
      {
        dispatcher = new ThreadPoolDispatcher( Executors.newFixedThreadPool( threads));
        clientContext.getModel().setDispatcher( dispatcher);
        Conventions.putCache( context, "org.xmodel.xaction.StartClientAction.dispatcher", dispatcher);
      }
      
      Client client = new Client( host, port, timeout, daemon);
      client.setServerContext( clientContext);
      Conventions.putCache( context, "org.xmodel.xaction.StartClientAction.client", client);
      
      StatefulContext stateful = (StatefulContext)context;
      IModelObject object = factory.createObject( null, "client");
      object.setValue( this);
      stateful.set( var, object);
      
      for( int i=1; i<=retries; i++)
      {
        try
        {
          if ( client.connect( timeout) != null) 
            break;
        }
        catch( IOException e)
        {
          if ( i == retries) throw e;
        }
        
        try { Thread.sleep( timeout);} catch( InterruptedException e2) {}
      }
    }
    catch( IOException e)
    {
      SLog.exception( this, e);
      Conventions.putCache( context, "org.xmodel.xaction.StartClientAction.client", null);
    }
    
    return null;
  }

  /**
   * @param context The context.
   * @return Returns null or the Client instance.
   */
  protected Client getClient( IContext context)
  {
    return (Client)Conventions.getCache( context, "org.xmodel.xaction.StartClientAction.client");
  }
  
  /**
   * Called by StopClientAction.
   * @param context The context.
   */
  protected void stop( IContext context) throws IOException
  {
    Client client = getClient( context);
    if ( client != null)
    {
      client.disconnect();
      Conventions.putCache( context, "org.xmodel.xaction.StartClientAction.client", null);
    }
    
    IDispatcher dispatcher = (IDispatcher)Conventions.getCache( context, "org.xmodel.xaction.StartClientAction.dispatcher");
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
  private IExpression retriesExpr;
  private IExpression contextExpr;
  private IExpression debugExpr;
  private IExpression daemonExpr;
  private IExpression threadsExpr;
  private IModelObjectFactory factory;
}
