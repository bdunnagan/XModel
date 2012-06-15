package org.xmodel.net;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.xmodel.IModelObject;
import org.xmodel.ThreadPoolDispatcher;
import org.xmodel.xml.XmlIO;
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

  @Test public void connectTimeoutTest() throws Exception
  {
    for( int i=0; i<connectLoops; i++)
    {
      Stopwatch sw = new Stopwatch();
      sw.start();
      
      try
      {
        Client client = new Client( host, port, timeout, true);
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
    Server server = new Server( host, port, timeout);
    server.setDispatcher( new ThreadPoolDispatcher( Executors.newFixedThreadPool( 1)));
    server.start( false);

    for( int i=0; i<3; i++)
    {
      Client client = new Client( host, port, timeout, true);
      Session session = client.connect( timeout, 0);
  
      Stopwatch sw = new Stopwatch();
      sw.start();
      
      try
      {
        IModelObject script = new XmlIO().read( "<script><sleep>3000</sleep></script>");
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
