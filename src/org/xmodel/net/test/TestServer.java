package org.xmodel.net.test;

import org.xmodel.IModelListener;
import org.xmodel.IModelObject;
import org.xmodel.ManualDispatcher;
import org.xmodel.ModelListener;
import org.xmodel.ModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.Server;
import org.xmodel.xpath.expression.StatefulContext;

public class TestServer
{
  public TestServer( IModelObject model)
  {
    this.model = model;
  }
  
  public void start() throws Exception
  {
    Thread thread = new Thread( "Test Server Model") {
      public void run()
      {
        model = model.cloneTree();
        
        try
        {
          server = new Server( "127.0.0.1", port, Integer.MAX_VALUE);
          server.start( true);
          
          ManualDispatcher dispatcher = new ManualDispatcher();
          model.getModel().setDispatcher( dispatcher);
          
          server.setServerContext( new StatefulContext( model));
          
          while( true)
          {
            dispatcher.process();
            Thread.sleep( 100);
          }
        }
        catch( Exception e)
        {
          log.exception( e);
        }
      }
    };
    
    thread.setDaemon( true);
    thread.start();
  }
  
  public void stop()
  {
    server.stop();
  }
  
  public static IModelObject buildConcurrentModificationModel( int count)
  {
    IModelListener listener = new ModelListener() {
      public void notifyChange( IModelObject object, String attrName, Object newObject, Object oldObject)
      {
        int newValue = (Integer)newObject;
        int oldValue = (Integer)oldObject;
        if ( (newValue - oldValue) != 1) 
        {
          log.error( "Detected modification by more than one!");
          System.exit( 1);
        }
      }
    };
    
    ModelObject model = new ModelObject( "model");
    for( int i=0; i<count; i++)
    {
      ModelObject child = new ModelObject( "child", ""+i);
      child.setAttribute( "name", "#"+i);
      child.setValue( 0);
      child.addModelListener( listener);
      model.addChild( child);
    }
    
    return model;
  }

  public final static int port = 9000;
  private static Log log = Log.getLog( TestServer.class);

  private Server server;
  private IModelObject model;
}
