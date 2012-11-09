package org.xmodel.net.nu;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import org.jboss.netty.channel.Channel;
import org.xmodel.IDispatcher;
import org.xmodel.IModelObject;
import org.xmodel.net.ICallback;
import org.xmodel.net.bind.BindProtocol;
import org.xmodel.net.bind.BindRequestProtocol.BindResult;
import org.xmodel.net.execution.ExecutionProtocol;
import org.xmodel.xpath.expression.IContext;

/**
 * This class provides methods that are common to both the client-side and the server-side of the protocol.
 */
public class Peer
{
  /**
   * Create a peer end-point with the specified configuration.
   * @param context The context.
   * @param dispatcher The dispatcher.
   * @param scheduler The scheduler used for protocol timers.
   */
  protected Peer( IContext context, IDispatcher dispatcher, ScheduledExecutorService scheduler)
  {
    bind = new BindProtocol( new HeaderProtocol(), new ErrorProtocol(), context, dispatcher);
    execute = new ExecutionProtocol( new HeaderProtocol(), new ErrorProtocol(), context, dispatcher, scheduler);
  }
  
  /**
   * Remotely bind the specified query.
   * @param readonly True if binding is readonly.
   * @param query The query to bind on the remote peer.
   * @param timeout The timeout in milliseconds to wait.
   * @return Returns the bind result.
   */
  public BindResult bind( boolean readonly, String query, int timeout) throws InterruptedException
  {
    return bind.bindRequestProtocol.send( channel, readonly, query, timeout);
  }
  
  /**
   * Unbind the query that returned the specified network identifier.
   * @param netID The network identifier of the query root element.
   */
  public void unbind( long netID) throws InterruptedException
  {
    bind.unbindRequestProtocol.send( channel, netID);
  }
  
  /**
   * Sync the remote element with the specified network identifier.
   * @param netID The network identifier of the remote element.
   * @param timeout The timeout in milliseconds.
   * @return Returns null or the sync'ed remote element.
   */
  public IModelObject sync( long netID, int timeout) throws InterruptedException
  {
    return bind.syncRequestProtocol.send( channel, netID, timeout);
  }

  
  /**
   * Remotely execute the specified operation synchronously.
   * @param context The local context.
   * @param vars Shared variables from the local context.
   * @param element The element representing the operation to execute.
   * @param timeout The timeout in milliseconds.
   * @return Returns the result.
   */
  public Object[] execute( IContext context, String[] vars, IModelObject element, int timeout) throws IOException, InterruptedException
  {
    return execute.requestProtocol.send( channel, context, vars, element, timeout);
  }
  
  /**
   * Remotely execute the specified operation asynchronously.
   * @param context The local context.
   * @param vars Shared variables from the local context.
   * @param element The element representing the operation to execute.
   * @param callback The callback.
   * @param timeout The timeout in milliseconds.
   */
  public void execute( IContext context, String[] vars, IModelObject element, ICallback callback, int timeout) throws IOException, InterruptedException
  {
    execute.requestProtocol.send( channel, context, vars, element, callback, timeout);
  }
  
  protected Channel channel;
  protected BindProtocol bind;
  protected ExecutionProtocol execute;
}
