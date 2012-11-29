package org.xmodel.net;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.xmodel.BlockingDispatcher;
import org.xmodel.IModelObject;
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
      
      XioClient client = new XioClient( null, null);
      client.connect( host, port).await( timeout);
      assertTrue( "Test-case error: connection was established.", client == null);
      
      sw.stop();
      assertTrue( "Connection timed-out late: "+sw.elapsed, sw.elapsed < 200);
    }
  }
  
  @Test public void executeTimeoutTest() throws Exception
  {
    StatefulContext context = new StatefulContext();
    XioServer server = new XioServer( context, context);
    server.start( host, port);

    for( int i=0; i<3; i++)
    {
      XioClient client = new XioClient( null, null);
      client.connect( host, port).await( timeout);
  
      Stopwatch sw = new Stopwatch();
      sw.start();
      
      try
      {
        IModelObject script = new XmlIO().read( "<script><sleep>3000</sleep></script>");
        client.execute( new StatefulContext(), new String[ 0], script, 500);
      }
      catch( IOException e)
      {
      }
      
      sw.stop();
      assertTrue( "Execution timed-out early.", sw.elapsed >= 500);
      assertTrue( "Execution did not timeout.", sw.elapsed < 3000);
      
      client.close();
    }
    
    server.stop();
  }
  
  @Test public void asyncExecuteSuccessTest() throws Exception
  {
    StatefulContext context = new StatefulContext();
    XioServer server = new XioServer( context, context);
    server.start( host, port);

    XioClient client = new XioClient( null, null);
    client.connect( host, port).await( timeout);

    IXAction onComplete = XActionDocument.parseScript( "<script><assign var='complete'>1</assign></script>");
    IXAction onSuccess = XActionDocument.parseScript( "<script><assign var='success'>1</assign></script>");
    IXAction onError = XActionDocument.parseScript( "<script><assign var='err'>1</assign></script>");
    
    for( int i=0; i<3; i++)
    {
      context = new StatefulContext();
      IModelObject script = new XmlIO().read( "<script><return>'June 23, 1912'</return></script>");

      Callback callback = new Callback( onComplete, onSuccess, onError);
      client.execute( context, new String[ 0], script, callback, Integer.MAX_VALUE);
  
      BlockingDispatcher dispatcher = (BlockingDispatcher)context.getModel().getDispatcher();
      dispatcher.process();
      
      assertTrue( "Incorrect result string.", context.get( "result").equals( "June 23, 1912"));
      assertTrue( "Complete script not executed.", context.get( "complete") != null);
      assertTrue( "Success script not executed.", context.get( "success") != null);
      assertTrue( "Error script should not have been executed.", context.get( "err") == null);
    }
    
    client.close();
    server.stop();
  }
  
  @Test public void asyncExecuteErrorTest() throws Exception
  {
    StatefulContext context = new StatefulContext();
    XioServer server = new XioServer( context, context);
    server.start( host, port);

    XioClient client = new XioClient( null, null);
    client.connect( host, port).await( timeout);

    IXAction onComplete = XActionDocument.parseScript( "<script><assign var='complete'>1</assign></script>");
    IXAction onSuccess = XActionDocument.parseScript( "<script><assign var='success'>1</assign></script>");
    IXAction onError = XActionDocument.parseScript( "<script><assign var='err'>1</assign></script>");
    
    for( int i=0; i<3; i++)
    {
      context = new StatefulContext();
      IModelObject script = new XmlIO().read( "<script><throw>'June 23, 1912'</throw></script>");
      
      Callback callback = new Callback( onComplete, onSuccess, onError);
      client.execute( context, new String[ 0], script, callback, Integer.MAX_VALUE);
  
      BlockingDispatcher dispatcher = (BlockingDispatcher)context.getModel().getDispatcher();
      dispatcher.process();
      
      assertTrue( "Result should not have been set.", context.get( "result") == null);
      assertTrue( "Error message not set.", context.get( "error") != null);
      assertTrue( "Incorrect error message.", context.get( "error").toString().contains( "June 23, 1912"));
      assertTrue( "Complete script not executed.", context.get( "complete") != null);
      assertTrue( "Error script not executed.", context.get( "err") != null);
      assertTrue( "Success script should not have been executed.", context.get( "success") == null);
    }
    
    client.close();
    server.stop();
  }
  
  @Test public void asyncExecuteTimeoutTest() throws Exception
  {
    StatefulContext context = new StatefulContext();
    XioServer server = new XioServer( context, context);
    server.start( host, port);

    XioClient client = new XioClient( null, null);
    client.connect( host, port).await( timeout);

    IXAction onComplete = XActionDocument.parseScript( "<script><assign var='complete'>1</assign></script>");
    IXAction onSuccess = XActionDocument.parseScript( "<script><assign var='success'>1</assign></script>");
    IXAction onError = XActionDocument.parseScript( "<script><assign var='err'>1</assign></script>");
    
    for( int i=0; i<3; i++)
    {
      context = new StatefulContext();
      IModelObject script = new XmlIO().read( "<script><sleep>200</sleep><return>'June 23, 1912'</return></script>");
      
      Callback callback = new Callback( onComplete, onSuccess, onError);
      client.execute( context, new String[ 0], script, callback, 1);
  
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
    
    client.close();
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
     * @see org.xmodel.net.IXioCallback#onSuccess(org.xmodel.xpath.expression.IContext, java.lang.Object[])
     */
    @Override
    public void onSuccess( IContext context, Object[] results)
    {
      if ( onSuccess != null) onSuccess.run( context);
    }

    /* (non-Javadoc)
     * @see org.xmodel.net.IXioCallback#onError(org.xmodel.xpath.expression.IContext, java.lang.String)
     */
    @Override
    public void onError( IContext context, String error)
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
