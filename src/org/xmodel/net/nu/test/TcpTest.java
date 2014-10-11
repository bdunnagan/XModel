package org.xmodel.net.nu.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import org.xmodel.IModelObject;
import org.xmodel.net.nu.DefaultEventHandler;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.net.nu.protocol.SimpleEnvelopeProtocol;
import org.xmodel.net.nu.protocol.XipWireProtocol;
import org.xmodel.net.nu.tcp.ITcpServerEventHandler;
import org.xmodel.net.nu.tcp.TcpClientTransport;
import org.xmodel.net.nu.tcp.TcpServerRouter;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

public class TcpTest
{
  public static void main( String[] args) throws Exception
  {
    class ServerEventHandler implements ITcpServerEventHandler
    {
      @Override
      public void notifyReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject requestMessage)
      {
        System.out.printf( "[SERVER] %s\n", XmlIO.write( Style.printable, message));
      }

      @Override
      public void notifyConnect( ITransport transport, IContext transportContext) throws IOException
      {
        System.out.println( "[SERVER] Client connected!");
      }

      @Override
      public void notifyDisconnect( ITransport transport, IContext transportContext) throws IOException
      {
        System.out.println( "[SERVER] Client disconnected!");
      }

      @Override
      public void notifyError( ITransport transport, IContext context, Error error, IModelObject request)
      {
        System.out.printf( "[SERVER] Error: %s\n", error);
      }

      @Override
      public void notifyException( ITransport transport, IOException e)
      {
        System.out.printf( "[SERVER] Exception: %s\n", e);
      }
    }
    
    class ClientEventHandler extends DefaultEventHandler
    {
      public ClientEventHandler( ITransport transport)
      {
        this.transport = transport;
      }
      
      @Override
      public boolean notifyReceive( ITransportImpl transport, IModelObject message, IContext messageContext, IModelObject requestMessage)
      {
        System.out.printf( "[CLIENT] %s\n", XmlIO.write( Style.printable, message));
        return false;
      }

      @Override
      public boolean notifyConnect( ITransportImpl transport, IContext transportContext) throws IOException
      {
        System.out.println( "[CLIENT] Connected!");
        
        try
        {
          transport.respond( new XmlIO().read( 
              "<message>"+
              "  <print>'Hi'</print>"+
              "</message>"
            ), null);
        }
        catch( XmlException e)
        {
          throw new IOException( e);
        }
        
        return false;
      }

      @Override
      public boolean notifyDisconnect( ITransportImpl transport, IContext transportContext) throws IOException
      {
        System.out.println( "[CLIENT] Disconnected!");
        return false;
      }

      @Override
      public boolean notifyError( ITransportImpl transport, IContext context, Error error, IModelObject request)
      {
        System.out.printf( "[CLIENT] Error: %s\n", error);
        return false;
      }

      @Override
      public boolean notifyException( ITransportImpl transport, IOException e)
      {
        System.out.printf( "[CLIENT] Exception: %s\n", e);
        return false;
      }
      
      private ITransport transport;
    }
    
    Protocol protocol = new Protocol( new XipWireProtocol(), new SimpleEnvelopeProtocol());
    
    System.out.println( "Starting server ...");
    IContext context = new StatefulContext();
    TcpServerRouter server = new TcpServerRouter( protocol, context, true);
    server.setEventHandler( new ServerEventHandler());
    server.start( new InetSocketAddress( "127.0.0.1", 10000));
    
    System.out.println( "Starting client ...");
    IContext clientContext = new StatefulContext();
    TcpClientTransport client = new TcpClientTransport( protocol, clientContext, null);
    client.setRemoteAddress( new InetSocketAddress( "127.0.0.1", 10000));
    
    client.getEventPipe().addLast( new ClientEventHandler( client));
    
    client.connect( 1000).await();
    
    System.out.println( "Sleeping ...");
    Thread.sleep( 100);
    
    System.out.println( "Disconnecting client ...");
    client.disconnect();
    
    System.out.println( "Sleeping ...");
    Thread.sleep( 100);
  }
}
