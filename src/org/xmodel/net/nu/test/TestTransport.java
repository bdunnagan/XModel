package org.xmodel.net.nu.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.net.nu.AbstractTransport;
import org.xmodel.net.nu.IErrorListener;
import org.xmodel.net.nu.IReceiveListener;
import org.xmodel.net.nu.ITransport;
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
  public TestTransport( Protocol protocol, IContext transportContext)
  {
    super( protocol, transportContext, null, null, null, null, null);
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
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope)
  {
    for( TestTransport transport: transports)
    {
      if ( transport == this) continue;
      try
      {
        byte[] bytes = getProtocol().wire().encode( envelope);
        transport.notifyReceive( bytes, 0, bytes.length);
      }
      catch( IOException e)
      {
        notifyError( getTransportContext(), ITransport.Error.encodeFailed);
      }
    }
    return new SuccessAsyncFuture<ITransport>( this);
  }
  
  private static List<TestTransport> transports = new ArrayList<TestTransport>();
  
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
    
    final TestTransport t1 = new TestTransport( protocol, context);
    t1.addListener( new IReceiveListener() {
      public void onReceive( final ITransport transport, final IModelObject message, IContext messageContext, final IModelObject request)
      {
        log.infof( "Transport #1:\nRequest:\n%s\nMessage:\n%s", 
          (request != null)? XmlIO.write( Style.printable, request): "null",
          XmlIO.write( Style.printable, message));
        
        if ( request != null && !message.getAttribute( "id").equals( request.getAttribute( "id")))
          throw new IllegalStateException();
       
        executor.execute( new Runnable() {
          public void run()
          {
            try
            {
              Thread.sleep( 50);
              StatefulContext messageContext = new StatefulContext( context);
              IModelObject next = new XmlIO().read( xml);
              transport.request( next, messageContext, 100);
            }
            catch( Exception e)
            {
              log.exception( e);
            }
          }
        });
      }
    });
    
    t1.addListener( new IErrorListener() {
      public void onError( ITransport transport, IContext context, ITransport.Error error)
      {
        log.infof( "Error #1");
      }
    });
    
    
    final TestTransport t2 = new TestTransport( protocol, context);
    t2.addListener( new IReceiveListener() {
      public void onReceive( final ITransport transport, final IModelObject message, IContext messageContext, final IModelObject request)
      {
        log.infof( "Transport #2:\nRequest:\n%s\nMessage:\n%s\n", 
          (request != null)? XmlIO.write( Style.printable, request): "null",
          XmlIO.write( Style.printable, message));
       
        if ( request != null && !message.getAttribute( "id").equals( request.getAttribute( "id")))
          throw new IllegalStateException();
        
        executor.execute( new Runnable() {
          public void run()
          {
            try
            {
              IModelObject next = new XmlIO().read( xml);
              Thread.sleep( 50);
              transport.respond( next, message);
            }
            catch( Exception e)
            {
              log.exception( e);
            }
          }
        });
      }
    });

    t2.addListener( new IErrorListener() {
      public void onError( ITransport transport, IContext context, ITransport.Error error)
      {
        log.infof( "Error #2");
      }
    });
    
    
    executor.execute( new Runnable() {
      public void run()
      {
        try
        {
          StatefulContext messageContext = new StatefulContext( context);
          IModelObject message = new XmlIO().read( xml);
          t1.request( message, messageContext, 100);
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
