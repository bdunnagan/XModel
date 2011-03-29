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
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ManualDispatcher;
import org.xmodel.Xlate;
import org.xmodel.log.Log;
import org.xmodel.net.ModelServer;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;


/**
 * An XAction which creates and starts a ModelServer.
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
    
    factory = getFactory( document.getRoot());
    
    // get assign
    assign = Xlate.get( document.getRoot(), "assign", (String)null);
    
    // get port and timeout
    port = Xlate.get( document.getRoot(), "port", ModelServer.defaultPort);
    
    // get context expression
    sourceExpr = document.getExpression();
    
    // create server
    server = new ModelServer( document.getRoot().getModel());
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    // get context
    IModelObject source = (sourceExpr != null)? sourceExpr.queryFirst( context): null;
    
    // start server
    try
    {
      server.setQueryContext( (source != null)? new StatefulContext( context.getScope(), source): context);
      server.start( port);
      
      StatefulContext stateful = (StatefulContext)context;
      IModelObject object = factory.createObject( null, "server");
      object.setValue( server);
      stateful.set( assign, object);
    }
    catch( IOException e)
    {
      log.exception( e);
    }

    // set dispatcher if none is defined
    IModel model = context.getModel();
    if ( model.getDispatcher() == null)
    {
      ManualDispatcher dispatcher = new ManualDispatcher();
      model.setDispatcher( dispatcher);
      while( true) 
      {
        dispatcher.process();
        try { Thread.sleep( 10);} catch( Exception e) { break;}
      }
    }
    
    return null;
  }
  
  private static Log log = Log.getLog( "org.xmodel.xaction");
  
  private IModelObjectFactory factory;
  private ModelServer server;
  private String assign;
  private int port;
  private IExpression sourceExpr;
}
