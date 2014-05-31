package org.xmodel.net.nu.run;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.future.AsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.net.nu.IReceiveListener;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

public class AsyncExecution implements IReceiveListener, Runnable
{
  public static final String timeout = "Timeout";
  public static final String cancelled = "Cancelled";
  
  public AsyncExecution( AsyncExecutionGroup group, ITransport transport)
  {
    this.group = group;
    this.transport = transport;
  }
  
  public AsyncFuture<ITransport> send( IModelObject script, ITransport transport, int timeout) throws IOException
  {
    this.request = script;
    
    transport.addListener( this);
    
    timeoutFuture = group.scheduler().schedule( this, timeout, TimeUnit.MILLISECONDS); 
    return transport.send( script, timeout);
  }

  @Override
  public final void onReceive( ITransport transport, IModelObject response, IContext context)
  {
    // message correlation
    String id = Xlate.get( response, "id", (String)null);
    if ( id == null || !id.equals( Xlate.get( request, "id", "")))
      return;
    
    try
    {
      // success/timeout exclusion
      if ( timeoutFuture.cancel( false))
      {
        StatefulContext successContext = new StatefulContext( context);
        group.notifySuccess( this, successContext, response); 
      }
    }
    catch( Exception e)
    {
      log.exception( e);
    }
    finally
    {
      transport.removeListener( this);
    }
  }

  @Override
  public void run()
  {
    transport.removeListener( this);
    
    IContext context = transport.getContexts().getMessageContext( request);
    StatefulContext timeoutContext = new StatefulContext( context);
    group.notifyError( this, timeoutContext, timeout);
  }

  public void cancel()
  {
    if ( request == null) return;
    
    transport.removeListener( this);
    
    if ( timeoutFuture.cancel( false))
    {
      IContext context = transport.getContexts().getMessageContext( request);
      StatefulContext cancelContext = new StatefulContext( context);
      group.notifyError( this, cancelContext, cancelled);
    }
  }
  
  public final static Log log = Log.getLog( AsyncExecution.class);

  private AsyncExecutionGroup group;
  private ITransport transport;
  private IModelObject request;
  private ScheduledFuture<?> timeoutFuture;
}
