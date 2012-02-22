package org.xmodel.net.test;

import java.util.ArrayList;
import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.Protocol;
import org.xmodel.net.Server;

public class NetTest
{
  public void test1() throws Exception
  {
    int count = 10;
    
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
      IModelObject clientModel = clientModels.get( i);
      clientModel.getChildren();
    }    

    // each client updates a different child
    for( int i=0; i<1000; i++)
    {
      for( int j=0; j<count; j++)
      {
        IModelObject clientModel = clientModels.get( j);
        IModelObject child = clientModel.getChild( j);
        child.setValue( i);
      }
    }
    
    log.info( "Test completed successfully.");
  }
  
  public static void main( String[] args) throws Exception
  {
    Log.getLog( Server.class).setLevel( Log.info);
    Log.getLog( Protocol.class).setLevel( Log.info);
    Log.getLog( NetTest.class).setLevel( Log.info);
    
    NetTest test = new NetTest();
    test.test1();
  }

  public static Log log = Log.getLog( NetTest.class);
}
