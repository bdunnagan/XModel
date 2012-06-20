package org.xmodel.xaction.debug;

import java.util.concurrent.Executors;
import org.junit.Test;
import org.xmodel.IModelObject;
import org.xmodel.ThreadPoolDispatcher;
import org.xmodel.net.Client;
import org.xmodel.net.Server;
import org.xmodel.net.Session;
import org.xmodel.xaction.XAction;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.StatefulContext;

public class DebuggerTest
{
  public final static String host = "localhost";
  public final static int port = 10000;
  public final static int timeout = 10000;
  
  @Test public void breakpointTest() throws Exception
  {
    System.setProperty( Debugger.debugProperty, "true");
    
    StatefulContext context = new StatefulContext();
    
    Server server = new Server( host, port, timeout);
    server.setServerContext( context);
    server.setDispatcher( new ThreadPoolDispatcher( Executors.newFixedThreadPool( 2)));
    server.start( false);
    
    IModelObject script = new XmlIO().read( 
        "<script>" +
        "  <assign var='x'>1</assign>" +
        "  <script>" +
        "    <breakpoint/>" +
        "    <assign var='x'>2</assign>" +
        "  </script>" +
        "  <assign var='x'>3</assign>" +
        "</script>"
    );

    Client client = new Client( host, port, timeout, false);
    Session session = client.connect( timeout);
    session.execute( new StatefulContext(), new String[ 0], script, 0);
    
    Debugger debugger = XAction.getDebugger();
    IModelObject stack = debugger.getStack();
    System.out.println( XmlIO.write( Style.printable, stack));
  }
}
