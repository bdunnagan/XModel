package org.xmodel.xaction.debug;

import java.util.concurrent.Semaphore;
import org.xmodel.concurrent.ThreadPoolDispatcher;
import org.xmodel.log.SLog;
import org.xmodel.net.Server;
import org.xmodel.xpath.expression.IContext;

/**
 * An implementation of IBreakHandler for network debugging.
 */
public class RemoteBreakHandler implements IBreakHandler
{
  public final static String debuggerPortProperty = "xaction.debug.port";
  
  public RemoteBreakHandler()
  {
    lock = new Semaphore( 0);
    
    try
    {
      int port = Integer.parseInt( System.getProperty( debuggerPortProperty));
      server = new Server( "localhost", port);
      server.setDispatcher( new ThreadPoolDispatcher( 1));
      server.start( true);
    }
    catch( Exception e)
    {
      SLog.exception( this, e);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IBreakHandler#breakpoint(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void breakpoint( IContext context)
  {
    try 
    { 
      lock.drainPermits();
      lock.acquire();
    } 
    catch( InterruptedException e) 
    {
      Thread.interrupted();
      SLog.warnf( this, "Debugger breakpoint interrupted.");
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IBreakHandler#resume()
   */
  @Override
  public void resume()
  {
    lock.release();
  }

  private Semaphore lock;
  private Server server;
}
