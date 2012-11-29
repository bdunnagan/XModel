package org.xmodel.xaction.debug;

import java.util.concurrent.Semaphore;

import org.xmodel.Model;
import org.xmodel.concurrent.SerialExecutorDispatcher;
import org.xmodel.log.SLog;
import org.xmodel.net.XioServer;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

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
      
      IContext context = new StatefulContext();
      context.getModel().setDispatcher( new SerialExecutorDispatcher( new Model(), 1));
      server = new XioServer( context, context);
      server.start( "localhost", port);
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
  private XioServer server;
}
