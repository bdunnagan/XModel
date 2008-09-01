package org.xmodel.net;

import java.util.List;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.ModelRegistry;
import org.xmodel.external.UnboundedCache;
import org.xmodel.xml.XmlIO;


public class TestClient
{
  public static void main( String[] args) throws Exception
  {
    IModel model = ModelRegistry.getInstance().getModel();
    BlockingDispatcher dispatcher = new BlockingDispatcher();
    model.setDispatcher( dispatcher);
    
    final ModelClient client = new ModelClient( "127.0.0.1", 17310, new UnboundedCache(), model);
    client.open();
    client.setQueryLimit( 10000);
    
    XmlIO xmlIO = new XmlIO();
    
    long t0 = System.nanoTime();
    List<IModelObject> result = client.bind( "collection('root')");
    long t1 = System.nanoTime();
    System.out.println( "ELAPSED: "+(t1-t0)/1000000f);
    
    System.out.println( xmlIO.write( result.get( 0)));
    
//    IModelObject expect = xmlIO.read( TestServer.class.getResource( "test.xml"));
//    XmlDiffer differ = new XmlDiffer();
//    ChangeSet changeSet = new ChangeSet();
//    differ.setMatcher( new ExactXmlMatcher() {
//      public boolean shouldDiff( IModelObject object, String attrName, boolean lhs)
//      {
//        if ( attrName == null) return true;
//        if ( attrName.startsWith( "net:")) return false;
//        return true;
//      }
//    });
//    
//    if ( !differ.diff( result.get( 0), expect, changeSet))
//    {
//      System.out.println( changeSet);
//      System.out.println( "fail.");
//      System.exit( 0);
//    }
//    
//    System.out.println( "pass.");

    while( true) dispatcher.process();
  }
}
