package org.xmodel.net.nu.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.IEventHandler;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.net.nu.protocol.SimpleEnvelopeProtocol;
import org.xmodel.net.nu.protocol.XipWireProtocol;
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
    class EventHandler implements IEventHandler
    {
      @Override
      public boolean notifyReceive( ByteBuffer buffer) throws IOException
      {
        return false;
      }

      @Override
      public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage)
      {
        System.out.printf( "[SERVER] %s\n", XmlIO.write( Style.printable, message));
        return false;
      }

      @Override
      public boolean notifyConnect() throws IOException
      {
        System.out.println( "[SERVER] Client connected!");
        return false;
      }

      @Override
      public boolean notifyDisconnect() throws IOException
      {
        System.out.println( "[SERVER] Client disconnected!");
        return false;
      }

      @Override
      public boolean notifyError( IContext context, Error error, IModelObject request)
      {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public boolean notifyException( IOException e)
      {
        // TODO Auto-generated method stub
        return false;
      }
    }
    
    Protocol protocol = new Protocol( new XipWireProtocol(), new SimpleEnvelopeProtocol());
    
    System.out.println( "Starting server ...");
    IContext context = new StatefulContext();
    TcpServerRouter server = new TcpServerRouter( protocol, context);
    server.addListener( new ConnectListener());
    server.addListener( new DisconnectListener());
    server.start( new InetSocketAddress( "127.0.0.1", 10000));
    
    System.out.println( "Starting client ...");
    IContext clientContext = new StatefulContext();
    TcpClientTransport client = new TcpClientTransport( protocol, clientContext, null, null, null, null, null);
    client.setRemoteAddress( new InetSocketAddress( "127.0.0.1", 10000));
    
    client.addListener( new IConnectListener() {
      public void onConnect( ITransport transport, IContext context) throws Exception
      {
        transport.respond( new XmlIO().read( 
            "<message>"+
            "  <print>'Hi'</print>"+
            "</message>"
          ), null);
      }
    });
    
    client.addListener( new IDisconnectListener() {
      public void onDisconnect( ITransport transport, IContext context) throws Exception
      {
        System.out.println( "[CLIENT] Disconnected!");
      }
    });
    
    client.connect( 1000).await();
    
    System.out.println( "Sleeping ...");
    Thread.sleep( 100);
    
    System.out.println( "Disconnecting client ...");
    client.disconnect();
    
    System.out.println( "Sleeping ...");
    Thread.sleep( 100);
  }
}
