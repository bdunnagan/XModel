package org.xmodel.net;

import java.io.IOException;
import org.jboss.netty.channel.Channel;
import org.xmodel.IModelObject;
import org.xmodel.external.IExternalReference;
import org.xmodel.net.bind.BindProtocol;
import org.xmodel.net.execution.ExecutionProtocol;
import org.xmodel.xpath.expression.IContext;

/**
 * This class represents an XIO protocol end-point.
 * (thread-safe)
 */
public class XioPeer
{
  /**
   * Create a peer end-point with the specified configuration.
   * @param handler The protocol handler.
   */
  XioPeer( XioChannelHandler handler)
  {
    this.handler = handler;
    bind = handler.getBindProtocol();
    execute = handler.getExecuteProtocol();
  }
  
  /**
   * Remotely bind the specified query.
   * @param reference The reference for which the bind is being performed.
   * @param readonly True if binding is readonly.
   * @param query The query to bind on the remote peer.
   * @param timeout The timeout in milliseconds to wait.
   */
  public void bind( IExternalReference reference, boolean readonly, String query, int timeout) throws InterruptedException
  {
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
    bind.bindRequestProtocol.send( reference, channel, readonly, query, timeout);
  }
  
  /**
   * Unbind the query that returned the specified network identifier.
   * @param netID The network identifier of the query root element.
   */
  public void unbind( int netID) throws InterruptedException
  {
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
    bind.unbindRequestProtocol.send( channel, netID);
  }
  
  /**
   * Sync the remote element with the specified network identifier.
   * @param netID The network identifier of the remote element.
   * @param timeout The timeout in milliseconds.
   * @return Returns null or the sync'ed remote element.
   */
  public IModelObject sync( int netID, int timeout) throws InterruptedException
  {
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
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
  public Object[] execute( IContext context, String[] vars, IModelObject element, int timeout) throws XioExecutionException, IOException, InterruptedException
  {
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
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
  public void execute( IContext context, String[] vars, IModelObject element, IXioCallback callback, int timeout) throws IOException, InterruptedException
  {
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
    execute.requestProtocol.send( channel, context, vars, element, callback, timeout);
  }
  
  protected Channel channel;
  protected XioChannelHandler handler;
  protected BindProtocol bind;
  protected ExecutionProtocol execute;
}
