/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import java.io.IOException;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ManualDispatcher;
import org.xmodel.Xlate;
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
  protected void doAction( IContext context)
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
      e.printStackTrace( System.err);
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
  }
  
  private IModelObjectFactory factory;
  private ModelServer server;
  private String assign;
  private int port;
  private IExpression sourceExpr;
}
