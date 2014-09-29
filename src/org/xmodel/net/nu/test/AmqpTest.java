package org.xmodel.net.nu.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.IEventHandler;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.amqp.AmqpTransport;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.net.nu.protocol.SimpleEnvelopeProtocol;
import org.xmodel.net.nu.protocol.XipWireProtocol;
import org.xmodel.net.nu.tcp.TcpClientTransport;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

public class AmqpTest
{
  public static void main( String[] args) throws Exception
  {
    class ClientEventHandler implements IEventHandler
    {
      public ClientEventHandler( ITransport transport)
      {
        this.transport = transport;
      }
      
      @Override
      public boolean notifyReceive( ByteBuffer buffer) throws IOException
      {
        return false;
      }

      @Override
      public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage)
      {
        System.out.printf( "[CLIENT] %s\n", XmlIO.write( Style.printable, message));
        return false;
      }

      @Override
      public boolean notifyConnect( IContext transportContext) throws IOException
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
      public boolean notifyDisconnect( IContext transportContext) throws IOException
      {
        System.out.println( "[CLIENT] Disconnected!");
        return false;
      }

      @Override
      public boolean notifyError( IContext context, Error error, IModelObject request)
      {
        System.out.printf( "[CLIENT] Error: %s\n", error);
        return false;
      }

      @Override
      public boolean notifyException( IOException e)
      {
        System.out.printf( "[CLIENT] Exception: %s\n", e);
        return false;
      }
      
      private ITransport transport;
    }
    
    Protocol protocol = new Protocol( new XipWireProtocol(), new SimpleEnvelopeProtocol());
    
    System.out.println( "Starting server ...");
    IContext context = new StatefulContext();
    AmqpTransport server = new AmqpTransport( protocol, context); 
    server.setRemoteAddress( new InetSocketAddress( "127.0.0.1", 5672));
    //server.getEventPipe().addLast( new ClientEventHandler( server));
    server.connect( 1000).await();
    
    System.out.println( "Starting client ...");
    IContext clientContext = new StatefulContext();
    AmqpTransport client = new AmqpTransport( protocol, clientContext);
    client.setRemoteAddress( new InetSocketAddress( "127.0.0.1", 5672));
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