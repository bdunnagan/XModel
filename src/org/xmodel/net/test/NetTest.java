package org.xmodel.net.test;

import java.util.ArrayList;
import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.ManualDispatcher;
import org.xmodel.ModelRegistry;
import org.xmodel.log.SLog;

public class NetTest
{
  public void test1() throws Exception
  {
    int count = 100;
    
    TestServer server = new TestServer( TestServer.buildConcurrentModificationModel( count));
    server.start();

    Thread.sleep( 1000);
    
    List<IModelObject> clientModels = new ArrayList<IModelObject>();
    for( int i=0; i<count; i++)
    {
      IModelObject clientModel = TestClient.createNetworkReference();
      clientModels.add( clientModel);
    }
    
    for( int i=0; i<count; i++)
    {
      long t0 = System.nanoTime();
      IModelObject clientModel = clientModels.get( i);
      clientModel.getChildren();
      long t1 = System.nanoTime();
      SLog.infof( this, "attach: %6.3fms", ((t1-t0) / 1000000f));
    }    

    System.exit( 0);
    
    // each client updates a different child
    for( int i=1; i<1; i++)
    {
      for( int j=0; j<count; j++)
      {
        IModelObject clientModel = clientModels.get( j);
        IModelObject child = clientModel.getChild( j);
        child.setValue( i);
      }
    }
    
    SLog.info( this, "Test completed successfully.");
  }
  
  public static void main( String[] args) throws Exception
  {
//    Log.getLog( Server.class).setLevel( Log.all);
//    Log.getLog( Protocol.class).setLevel( Log.all);
//    Log.getLog( NetTest.class).setLevel( Log.all);
    
    ManualDispatcher dispatcher = new ManualDispatcher();
    ModelRegistry.getInstance().getModel().setDispatcher( dispatcher);
    
    NetTest test = new NetTest();
    test.test1();
  }
}
