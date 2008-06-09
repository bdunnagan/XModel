/*
 * Stonewall Networks, Inc.
 *
 *   Project: XModel
 *   Author:  bdunnagan
 *   Date:    Apr 16, 2008
 *
 * Copyright 2008.  Stonewall Networks, Inc.
 */
package dunnagan.bob.xmodel.xaction;

import java.io.IOException;

import dunnagan.bob.xmodel.IModel;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.net.ManualDispatcher;
import dunnagan.bob.xmodel.net.ModelServer;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;
import dunnagan.bob.xmodel.xpath.expression.StatefulContext;

/**
 * An XAction which creates and starts a ModelServer.
 */
public class StartServerAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#configure(dunnagan.bob.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
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
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
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
      IModelObject object = new ModelObject( "server");
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
  
  private ModelServer server;
  private String assign;
  private int port;
  private IExpression sourceExpr;
}
