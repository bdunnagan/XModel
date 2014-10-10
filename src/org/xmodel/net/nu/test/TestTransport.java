package org.xmodel.net.nu.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.FailureAsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.net.nu.AbstractTransport;
import org.xmodel.net.nu.DefaultEventHandler;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.algo.ReliableAlgo;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.net.nu.protocol.SimpleEnvelopeProtocol;
import org.xmodel.net.nu.protocol.XmlWireProtocol;
import org.xmodel.util.PrefixThreadFactory;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

public class TestTransport extends AbstractTransport
{
  public TestTransport( Protocol protocol, IContext transportContext, int fail)
  {
    super( protocol, transportContext, null);
    this.count = fail;
    transports.add( this);
  }

  @Override
  public AsyncFuture<ITransport> connect( int timeout)
  {
    return null;
  }

  @Override
  public AsyncFuture<ITransport> disconnect()
  {
    return null;
  }

  @Override
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope, IModelObject request)
  {
    if ( count-- == 0) 
    {
      getEventPipe().notifyError( getTransportContext(), ITransport.Error.channelClosed, envelope);
      return new FailureAsyncFuture<ITransport>( this, "dummy");
    }
    
    for( TestTransport transport: transports)
    {
      if ( transport == this) continue;
      try
      {
        byte[] bytes = getProtocol().wire().encode( envelope);
        transport.getEventPipe().notifyReceive( ByteBuffer.wrap( bytes, 0, bytes.length));
      }
      catch( IOException e)
      {
        transport.getEventPipe().notifyError( getTransportContext(), ITransport.Error.encodeFailed, null);
      }
    }
    return new SuccessAsyncFuture<ITransport>( this);
  }
  
  private static List<TestTransport> transports = new ArrayList<TestTransport>();
  
  private int count;
  
  public static void main( String[] args) throws Exception
  {
    final Log log = Log.getLog( "Test");
    
    final String xml =
      "<message>"+
      "  <print>'Hi'</print>"+
      "</message>";

    final ExecutorService executor = Executors.newFixedThreadPool( 4, new PrefixThreadFactory( "worker"));
    final StatefulContext context = new StatefulContext();
    
    Protocol protocol = new Protocol( new XmlWireProtocol(), new SimpleEnvelopeProtocol());
    
    final ReliableAlgo t1 = new ReliableAlgo( new TestTransport( protocol, context, -1));
    t1.getEventPipe().addLast( new DefaultEventHandler() {
      public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject request)
      {
        log.infof( "Transport #1:\nRequest:\n%s\nMessage:\n%s", 
          (request != null)? XmlIO.write( Style.printable, request): "null",
          XmlIO.write( Style.printable, message));
        
//        executor.execute( new Runnable() {
//          public void run()
//          {
//            try
//            {
//              Thread.sleep( 50);
//              StatefulContext messageContext = new StatefulContext( context);
//              IModelObject next = new XmlIO().read( xml);
//              transport.request( next, messageContext, 1000);
//            }
//            catch( Exception e)
//            {
//              log.exception( e);
//            }
//          }
//        });
        
        return false;
      }
      
      public boolean notifyError( IContext context, ITransport.Error error, IModelObject request)
      {
        log.infof( "Error #1: %s", error);
        return false;
      }
    });
    
    
    final ITransport t2 = new ReliableAlgo( new TestTransport( protocol, context, 0));
    t2.getEventPipe().addLast( new DefaultEventHandler() {
      public boolean notifyReceive( final IModelObject message, IContext messageContext, final IModelObject request)
      {
        log.infof( "Transport #2:\nRequest:\n%s\nMessage:\n%s\n", 
          (request != null)? XmlIO.write( Style.printable, request): "null",
          XmlIO.write( Style.printable, message));
       
        executor.execute( new Runnable() {
          public void run()
          {
            try
            {
              IModelObject next = new XmlIO().read( xml);
              Thread.sleep( 1000);
              t2.respond( next, message);
            }
            catch( Exception e)
            {
              log.exception( e);
            }
          }
        });
        
        return false;
      }
      
      public boolean notifyError( IContext context, ITransport.Error error, IModelObject request)
      {
        log.infof( "Error #2: %s", error);
        return false;
      }
    });

    executor.execute( new Runnable() {
      public void run()
      {
        try
        {
          StatefulContext messageContext = new StatefulContext( context);
          IModelObject message = new XmlIO().read( xml);
          t1.request( message, messageContext, 1000, Integer.MAX_VALUE);
        }
        catch( Exception e)
        {
          log.exception( e);
        }
      }
    });

    Thread.sleep( 100000);
  }
}
