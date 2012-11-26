package org.xmodel.net;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.xmodel.BlockingDispatcher;
import org.xmodel.INode;
import org.xmodel.concurrent.SerialExecutorDispatcher;
import org.xmodel.net.IXioCallback;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * Test case for many clients accessing a single server.
 */
public class ProtocolTimeoutTest
{
  public final static String host = "localhost";
  public final static int port = 10000;
  public final static int timeout = 10000;
  public final static int connectLoops = 3;

  @Before public void start() throws IOException
  {
//    Log.getLog( TcpBase.class).setLevel( Log.all);
//    Log.getLog( Connection.class).setLevel( Log.all);
//    Log.getLog( Client.class).setLevel( Log.all);
//    Log.getLog( Server.class).setLevel( Log.all);
//    Log.getLog( Protocol.class).setLevel( Log.all);
  }
  
  @Test public void connectTimeoutTest() throws Exception
  {
    for( int i=0; i<connectLoops; i++)
    {
      Stopwatch sw = new Stopwatch();
      sw.start();
      
      try
      {
        XioClient client = new XioClient( host, port, true);
        client.setPingTimeout( timeout);
        Session session = client.connect( 100, 0);
        assertTrue( "Test-case error: connection was established.", session == null);
      }
      catch( IOException e)
      {
      }
      
      sw.stop();
      assertTrue( "Connection timed-out late: "+sw.elapsed, sw.elapsed < 200);
    }
  }
  
  @Test public void executeTimeoutTest() throws Exception
  {
    XioServer server = new XioServer( host, port);
    server.setPingTimeout( timeout);
    server.setDispatcher( new SerialExecutorDispatcher( 1));
    server.start( false);

    for( int i=0; i<3; i++)
    {
      XioClient client = new XioClient( host, port, true);
      client.setPingTimeout( timeout);
      Session session = client.connect( timeout, 0);
  
      Stopwatch sw = new Stopwatch();
      sw.start();
      
      try
      {
        INode script = new XmlIO().read( "<script><sleep>3000</sleep></script>");
        session.execute( new StatefulContext(), new String[ 0], script, 500);
      }
      catch( IOException e)
      {
      }
      
      sw.stop();
      assertTrue( "Execution timed-out early.", sw.elapsed >= 500);
      assertTrue( "Execution did not timeout.", sw.elapsed < 3000);
      
      client.disconnect();
    }
    
    server.stop();
  }
  
  @Test public void asyncExecuteSuccessTest() throws Exception
  {
    XioServer server = new XioServer( host, port);
    server.setPingTimeout( timeout);
    server.setDispatcher( new SerialExecutorDispatcher( 1));
    server.start( false);

    XioClient client = new XioClient( host, port, true);
    client.setPingTimeout( timeout);
    Session session = client.connect( timeout, 0);

    IXAction onComplete = XActionDocument.parseScript( "<script><assign var='complete'>1</assign></script>");
    IXAction onSuccess = XActionDocument.parseScript( "<script><assign var='success'>1</assign></script>");
    IXAction onError = XActionDocument.parseScript( "<script><assign var='err'>1</assign></script>");
    
    for( int i=0; i<3; i++)
    {
      StatefulContext context = new StatefulContext();
      INode script = new XmlIO().read( "<script><return>'June 23, 1912'</return></script>");

      Callback callback = new Callback( onComplete, onSuccess, onError);
      session.execute( context, new String[ 0], script, callback, Integer.MAX_VALUE);
  
      BlockingDispatcher dispatcher = (BlockingDispatcher)context.getModel().getDispatcher();
      dispatcher.process();
      
      assertTrue( "Incorrect result string.", context.get( "result").equals( "June 23, 1912"));
      assertTrue( "Complete script not executed.", context.get( "complete") != null);
      assertTrue( "Success script not executed.", context.get( "success") != null);
      assertTrue( "Error script should not have been executed.", context.get( "err") == null);
    }
    
    client.disconnect();
    server.stop();
  }
  
  @Test public void asyncExecuteErrorTest() throws Exception
  {
    XioServer server = new XioServer( host, port);
    server.setPingTimeout( timeout);
    server.setDispatcher( new SerialExecutorDispatcher( 1));
    server.start( false);

    XioClient client = new XioClient( host, port, true);
    client.setPingTimeout( timeout);
    Session session = client.connect( timeout, 0);

    IXAction onComplete = XActionDocument.parseScript( "<script><assign var='complete'>1</assign></script>");
    IXAction onSuccess = XActionDocument.parseScript( "<script><assign var='success'>1</assign></script>");
    IXAction onError = XActionDocument.parseScript( "<script><assign var='err'>1</assign></script>");
    
    for( int i=0; i<3; i++)
    {
      StatefulContext context = new StatefulContext();
      INode script = new XmlIO().read( "<script><throw>'June 23, 1912'</throw></script>");
      
      Callback callback = new Callback( onComplete, onSuccess, onError);
      session.execute( context, new String[ 0], script, callback, Integer.MAX_VALUE);
  
      BlockingDispatcher dispatcher = (BlockingDispatcher)context.getModel().getDispatcher();
      dispatcher.process();
      
      assertTrue( "Result should not have been set.", context.get( "result") == null);
      assertTrue( "Error message not set.", context.get( "error") != null);
      assertTrue( "Incorrect error message.", context.get( "error").toString().contains( "June 23, 1912"));
      assertTrue( "Complete script not executed.", context.get( "complete") != null);
      assertTrue( "Error script not executed.", context.get( "err") != null);
      assertTrue( "Success script should not have been executed.", context.get( "success") == null);
    }
    
    client.disconnect();
    server.stop();
  }
  
  @Test public void asyncExecuteTimeoutTest() throws Exception
  {
    XioServer server = new XioServer( host, port);
    server.setPingTimeout( timeout);
    server.setDispatcher( new SerialExecutorDispatcher( 1));
    server.start( false);

    XioClient client = new XioClient( host, port, true);
    client.setPingTimeout( timeout);
    Session session = client.connect( timeout, 0);

    IXAction onComplete = XActionDocument.parseScript( "<script><assign var='complete'>1</assign></script>");
    IXAction onSuccess = XActionDocument.parseScript( "<script><assign var='success'>1</assign></script>");
    IXAction onError = XActionDocument.parseScript( "<script><assign var='err'>1</assign></script>");
    
    for( int i=0; i<3; i++)
    {
      StatefulContext context = new StatefulContext();
      INode script = new XmlIO().read( "<script><sleep>200</sleep><return>'June 23, 1912'</return></script>");
      
      Callback callback = new Callback( onComplete, onSuccess, onError);
      session.execute( context, new String[ 0], script, callback, 1);
  
      try { Thread.sleep( 500);} catch( Exception e) {}
      BlockingDispatcher dispatcher = (BlockingDispatcher)context.getModel().getDispatcher();
      dispatcher.process();
      
      assertTrue( "Result should not have been set.", context.get( "result") == null);
      assertTrue( "Error not set.", context.get( "error") != null);
      assertTrue( "Incorrect error message.", context.get( "error").toString().equals( "timeout"));
      assertTrue( "Complete script not executed.", context.get( "complete") != null);
      assertTrue( "Error script not executed.", context.get( "err") != null);
      assertTrue( "Success script should not have been executed.", context.get( "success") == null);
    }
    
    client.disconnect();
    server.stop();
  }
  
  private final static class Callback implements IXioCallback
  {
    public Callback( IXAction onComplete, IXAction onSuccess, IXAction onError)
    {
      this.onComplete = onComplete;
      this.onSuccess = onSuccess;
      this.onError = onError;
    }
    
    /* (non-Javadoc)
     * @see org.xmodel.net.ICallback#onComplete(org.xmodel.xpath.expression.IContext)
     */
    @Override
    public void onComplete( IContext context)
    {
      if ( onComplete != null) onComplete.run( context);
    }

    /* (non-Javadoc)
     * @see org.xmodel.net.ICallback#onSuccess(org.xmodel.xpath.expression.IContext)
     */
    @Override
    public void onSuccess( IContext context)
    {
      if ( onSuccess != null) onSuccess.run( context);
    }

    /* (non-Javadoc)
     * @see org.xmodel.net.ICallback#onError(org.xmodel.xpath.expression.IContext)
     */
    @Override
    public void onError( IContext context)
    {
      if ( onError != null) onError.run( context);
    }
    
    private IXAction onComplete;
    private IXAction onSuccess;
    private IXAction onError;
  }
  
  private static final class Stopwatch
  {
    public void start()
    {
      start = System.nanoTime();
    }
    
    public void stop()
    {
      finish = System.nanoTime();
      elapsed = (finish - start) / 1000000.0; 
    }
    
    public long start;
    public long finish;
    public double elapsed;
  }
}
