package org.xmodel.net.nu.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.net.nu.AbstractTransport;
import org.xmodel.net.nu.IProtocol;
import org.xmodel.net.nu.IReceiveListener;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.protocol.XmlProtocol;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

public class TestTransport extends AbstractTransport
{
  public TestTransport( IProtocol protocol, IContext transportContext)
  {
    super( protocol, transportContext, Executors.newScheduledThreadPool( 1));
    transports.add( this);
  }

  @Override
  public AsyncFuture<ITransport> connect( int timeout) throws IOException
  {
    return null;
  }

  @Override
  public AsyncFuture<ITransport> disconnect() throws IOException
  {
    return null;
  }

  @Override
  public AsyncFuture<ITransport> send( IModelObject message) throws IOException
  {
    for( TestTransport transport: transports)
    {
      if ( transport == this) continue;
      byte[] bytes = getProtocol().encode( message);
      transport.notifyReceive( bytes, 0, bytes.length);
    }
    return new SuccessAsyncFuture<ITransport>( this);
  }
  
  private static List<TestTransport> transports = new ArrayList<TestTransport>();
  
  public static void main( String[] args) throws Exception
  {
    String xml =
      "<message>"+
      "  <print>'Hi'</print>"+
      "</message>";

    IModelObject message = new XmlIO().read( xml);
    
    StatefulContext context = new StatefulContext();
    
    TestTransport t1 = new TestTransport( new XmlProtocol(), context);
    t1.addListener( new IReceiveListener() {
      public void onReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject request)
      {
        System.out.printf( "Transport #1:\n%s", XmlIO.write( Style.printable, message));
      }
    });
    
    TestTransport t2 = new TestTransport( new XmlProtocol(), context);
    t2.addListener( new IReceiveListener() {
      public void onReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject request)
      {
        System.out.printf( "Transport #2:\n%s", XmlIO.write( Style.printable, message));
      }
    });

    t1.send( message);
  }
}
