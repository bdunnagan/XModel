package org.xmodel.net;

import static org.junit.Assert.assertTrue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmodel.BlockingDispatcher;
import org.xmodel.INode;
import org.xmodel.ModelObject;
import org.xmodel.external.ICachingPolicy;
import org.xmodel.external.IExternalReference;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class GCTest
{
  @Before
  public void setUp() throws Exception
  {
    String xml = 
        "<script>" +
        "  <create var='ref'>" +
        "    <ref>" +
        "      <extern:cache class=\"org.xmodel.net.NetworkCachingPolicy\" dirty=\"true\">" + 
        "        <host>localhost</host>" + 
        "        <port>33333</port>" + 
        "        <timeout>30000</timeout>" + 
        "        <query>.</query>" + 
        "      </extern:cache>" +
        "    </ref>" + 
        "  </create>" +
        "</script>";
    
    clientContext = new StatefulContext();
    
    XActionDocument doc = new XActionDocument( new XmlIO().read( xml));
    script = doc.createScript();
    script.run( clientContext);
    

    INode serverRoot = new ModelObject( "server");
    serverRoot.getCreateChild( "child");
    
    server = new XioServer( "localhost", 33333);
    server.setServerContext( new StatefulContext( serverRoot));
    server.start( true);
    
    Thread thread = new Thread( "Server") {
      public void run()
      {
        try
        {
          BlockingDispatcher dispatcher = (BlockingDispatcher)server.getDispatcher();
          while( dispatcher.process());
        }
        catch( Exception e)
        {
          e.printStackTrace( System.err);
        }
      }
    };
    
    thread.start();
  }

  @After
  public void tearDown() throws Exception
  {
    server.stop();
  }

  @Test
  public void testGC() throws Exception
  {
    // sync reference
    IExpression refExpr = XPath.createExpression( "$ref");
    
    IExternalReference ref = (IExternalReference)refExpr.queryFirst( clientContext);
    ref.getChildren();

    // remove reference from context
    clientContext.set( "ref", Collections.<INode>emptyList());
 
    // forcefully disconnect caching policy
    NetworkCachingPolicy cp = (NetworkCachingPolicy)ref.getCachingPolicy();
    cp.getClient().disconnect();
    
    // verify cleanup
    WeakReference<ICachingPolicy> weakRef1 = new WeakReference<ICachingPolicy>( ref.getCachingPolicy());
    assertTrue( weakRef1.get() != null);
    
    WeakReference<IExternalReference> weakRef2 = new WeakReference<IExternalReference>( ref);
    assertTrue( weakRef2.get() != null);
    
    ref = null;
    
    System.gc();
    System.gc();
    System.gc();

    assertTrue( weakRef2.get() == null);
    assertTrue( weakRef1.get() == null);
  }
  
  private StatefulContext clientContext;
  private IXAction script;
  private XioServer server;
}
