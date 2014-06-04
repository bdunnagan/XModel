package org.xmodel.net.nu.test;

import java.net.InetSocketAddress;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.IConnectListener;
import org.xmodel.net.nu.IDisconnectListener;
import org.xmodel.net.nu.IReceiveListener;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.protocol.BasicEnvelopeProtocol;
import org.xmodel.net.nu.protocol.XmlWireProtocol;
import org.xmodel.net.nu.tcp.TcpClientTransport;
import org.xmodel.net.nu.tcp.TcpServerRouter;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

public class TcpTest
{
  public static void main( String[] args) throws Exception
  {
    class ReceiveListener implements IReceiveListener
    {
      @Override
      public void onReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject request)
      {
        System.out.println( XmlIO.write( Style.printable, message));
      }
    }
    
    class ConnectListener implements IConnectListener
    {
      @Override
      public void onConnect( ITransport transport, IContext context)
      {
        transport.addListener( new ReceiveListener());
      }
    }
    
    System.out.println( "Starting server ...");
    IContext context = new StatefulContext();
    TcpServerRouter server = new TcpServerRouter( new XmlWireProtocol(), new BasicEnvelopeProtocol(), context);
    server.addListener( new ConnectListener());
    server.start( new InetSocketAddress( "0.0.0.0", 10000));
    
    System.out.println( "Starting client ...");
    IContext clientContext = new StatefulContext();
    TcpClientTransport client = new TcpClientTransport( new XmlWireProtocol(), new BasicEnvelopeProtocol(), clientContext, null, null, null, null, null);
    client.setRemoteAddress( new InetSocketAddress( "127.0.0.1", 10000));
    
    client.addListener( new IConnectListener() {
      public void onConnect( ITransport transport, IContext context) throws Exception
      {
        transport.send( new XmlIO().read( 
            "<message>"+
            "  <print>'Hi'</print>"+
            "</message>"
          ));
      }
    });
    
    client.addListener( new IDisconnectListener() {
      public void onDisconnect( ITransport transport, IContext context) throws Exception
      {
        System.out.println( "Client disconnected!");
      }
    });
    
    client.connect( 1000).await();
    
    System.out.println( "Sleeping ...");
    Thread.sleep( 1000);
    
    System.out.println( "Disconnecting client ...");
    client.disconnect();
    
    System.out.println( "Sleeping ...");
    Thread.sleep( 1000);
  }
}
