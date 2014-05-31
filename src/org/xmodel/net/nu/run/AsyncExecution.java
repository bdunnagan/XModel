package org.xmodel.net.nu.run;

import java.io.IOException;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.net.nu.IReceiveListener;
import org.xmodel.net.nu.ITimeoutListener;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xpath.expression.IContext;

public class AsyncExecution implements IReceiveListener, ITimeoutListener
{
  public static final String timeoutMessage = "Timeout";
  public static final String cancelledMessage = "Cancelled";
  
  public AsyncExecution( AsyncExecutionGroup group, ITransport transport)
  {
    this.group = group;
    this.transport = transport;
  }
  
  public AsyncFuture<ITransport> send( IModelObject script, IContext messageContext, ITransport transport, int timeout) throws IOException
  {
    addListeners();
    return transport.send( script, messageContext, timeout);
  }

  @Override
  public final void onReceive( ITransport transport, IModelObject response, IContext messageContext, IModelObject request)
  {
    try
    {
      group.notifySuccess( this, messageContext, response); 
    }
    catch( Exception e)
    {
      log.exception( e);
    }
    finally
    {
      removeListeners();
    }
  }
  
  @Override
  public void onTimeout( ITransport transport, IModelObject message, IContext messageContext)
  {
    try
    {
      group.notifyError( this, messageContext, timeoutMessage); 
    }
    catch( Exception e)
    {
      log.exception( e);
    }
    finally
    {
      removeListeners();
    }
  }

  private void addListeners()
  {
    transport.addListener( (IReceiveListener)this);
    transport.addListener( (ITimeoutListener)this);
  }
  
  private void removeListeners()
  {
    transport.removeListener( (IReceiveListener)this);
    transport.removeListener( (ITimeoutListener)this);
  }
  
  public final static Log log = Log.getLog( AsyncExecution.class);

  private AsyncExecutionGroup group;
  private ITransport transport;
}
