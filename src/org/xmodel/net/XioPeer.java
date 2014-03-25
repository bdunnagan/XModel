package org.xmodel.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import org.jboss.netty.buffer.ChannelBuffer;
import org.xmodel.GlobalSettings;
import org.xmodel.IModelObject;
import org.xmodel.external.IExternalReference;
import org.xmodel.future.AsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.net.HeaderProtocol.Type;
import org.xmodel.net.bind.BindProtocol;
import org.xmodel.net.connection.INetworkConnection;
import org.xmodel.net.echo.EchoProtocol;
import org.xmodel.net.execution.ExecutionPrivilege;
import org.xmodel.net.execution.ExecutionProtocol;
import org.xmodel.net.register.RegisterProtocol;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * This class represents an XIO protocol end-point.
 * (thread-safe)
 */
public class XioPeer
{
  /**
   * Create an XioPeer with the specified parameters.
   * @param channel Null or the transport channel.
   * @param registry Null or the local peer registry.
   * @param context The context for the protocol.
   * @param executor The executor.
   * @param scheduler The scheduler.
   * @param privilege The execution privilege manager.
   */
  protected XioPeer( 
      IXioChannel channel, 
      IXioPeerRegistry registry, 
      IContext context, 
      Executor executor, 
      ScheduledExecutorService scheduler, 
      ExecutionPrivilege privilege)
  {
    this.channel = channel;
    this.registry = registry;
    this.eventContext = new StatefulContext( context);
    
    if ( scheduler == null) scheduler = GlobalSettings.getInstance().getScheduler();
    
    headerProtocol = new HeaderProtocol();
    echoProtocol = new EchoProtocol( headerProtocol, executor);
    registerProtocol = new RegisterProtocol( registry, headerProtocol);
    bindProtocol = new BindProtocol( headerProtocol, context, executor);
    executionProtocol = new ExecutionProtocol( headerProtocol, context, executor, scheduler, privilege);
  }
  
  /**
   * Specify whether the peer should attempt to reconnect if a message is sent and the underlying channel
   * is no longer connected.  Only one retry attempt will be made.
   * @param reconnect True if connection should be retried.
   */
  public void setReconnect( boolean reconnect)
  {
    // TODO: add reconnect behavior to NettyXioChannel
  }

  /**
   * Send a heartbeat.
   */
  public void heartbeat() throws IOException
  {
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
    echoProtocol.requestProtocol.send( channel);
  }
  
  /**
   * Register this peer under the specified name with the remote endpoint.
   * @param name The name to be associated with this peer.
   */
  public void register( final String name) throws IOException, InterruptedException
  {
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
    registerProtocol.registerRequestProtocol.send( channel, name);
  }
  
  /**
   * Unregister all the names of this peer with the remote endpoint.
   * @param name The name previously associated with this peer.
   */
  public void unregisterAll() throws IOException, InterruptedException
  {
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
    registerProtocol.unregisterRequestProtocol.send( channel);
  }
  
  /**
   * Unregister the specified name of this peer with the remote endpoint.
   * @param name The name previously associated with this peer.
   */
  public void unregister( final String name) throws IOException, InterruptedException
  {
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
    registerProtocol.unregisterRequestProtocol.send( channel, name);
  }
  
  /**
   * Remotely bind the specified query.
   * @param reference The reference for which the bind is being performed.
   * @param readonly True if binding is readonly.
   * @param query The query to bind on the remote peer.
   * @param timeout The timeout in milliseconds to wait.
   */
  public void bind( final IExternalReference reference, final boolean readonly, final String query, final int timeout) throws InterruptedException
  {
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
    bindProtocol.bindRequestProtocol.send( reference, channel, readonly, query, timeout);
  }
  
  /**
   * Unbind the query that returned the specified network identifier.
   * @param netID The network identifier of the query root element.
   */
  public void unbind( final int netID) throws InterruptedException
  {
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
    bindProtocol.unbindRequestProtocol.send( channel, netID);
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
    return bindProtocol.syncRequestProtocol.send( channel, netID, timeout);
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
    return executionProtocol.requestProtocol.send( channel, context, vars, element, timeout);
  }
  
  /**
   * Remotely execute the specified operation asynchronously.
   * @param context The local context.
   * @param correlation The correlation number.
   * @param vars Shared variables from the local context.
   * @param element The element representing the operation to execute.
   * @param callback The callback.
   * @param timeout The timeout in milliseconds.
   */
  public void execute( IContext context, String[] vars, IModelObject element, IXioCallback callback, int timeout) throws IOException, InterruptedException
  {
    IXioChannel channel = getChannel();
    if ( channel == null) throw new IllegalStateException( "Peer is not connected.");
    executionProtocol.requestProtocol.send( channel, context, vars, element, callback, timeout);
  }
  
  /**
   * Read the next message from the buffer and pass it on for processing.
   * @param channel The channel.
   * @param buffer The buffer.
   * @return Returns true if a message was read.
   */
  public boolean handleMessage( IXioChannel channel, ChannelBuffer buffer) throws IOException
  {
    if ( log.verbose()) log.verbosef( "handleMessage: offset=%d\n%s", buffer.readerIndex(), toString( "  ", buffer));
    
    if ( buffer.readableBytes() < 9) return false;
    
    Type type = headerProtocol.readType( buffer);
    log.debugf( "Message Type: %s", type);
    
    long length = headerProtocol.readLength( buffer);
    if ( buffer.readableBytes() < length) return false;
    log.debugf( "Message Length: %d", length);
    
    switch( type)
    {
      case echoRequest:     echoProtocol.requestProtocol.handle( channel, buffer); return true;
      case echoResponse:    echoProtocol.responseProtocol.handle( channel, buffer); return true;
      
      case executeRequest:  executionProtocol.requestProtocol.handle( channel, buffer); return true;
      case cancelRequest:   executionProtocol.requestProtocol.handleCancel( channel, buffer); return true;
      case executeResponse: executionProtocol.responseProtocol.handle( channel, buffer); return true;
      
      case bindRequest:     bindProtocol.bindRequestProtocol.handle( channel, buffer, length); return true;
      case bindResponse:    bindProtocol.bindResponseProtocol.handle( channel, buffer, length); return true;
      case unbindRequest:   bindProtocol.unbindRequestProtocol.handle( channel, buffer); return true;
      case syncRequest:     bindProtocol.syncRequestProtocol.handle( channel, buffer); return true;
      case syncResponse:    bindProtocol.syncResponseProtocol.handle( channel, buffer); return true;
      case addChild:        bindProtocol.updateProtocol.handleAddChild( channel, buffer); return true;
      case removeChild:     bindProtocol.updateProtocol.handleRemoveChild( channel, buffer); return true;
      case changeAttribute: bindProtocol.updateProtocol.handleChangeAttribute( channel, buffer); return true;
      case clearAttribute:  bindProtocol.updateProtocol.handleClearAttribute( channel, buffer); return true;
      case changeDirty:     bindProtocol.updateProtocol.handleChangeDirty( channel, buffer); return true;
      
      case register:        registerProtocol.registerRequestProtocol.handle( channel, buffer); return true;
      case unregister:      registerProtocol.unregisterRequestProtocol.handle( channel, buffer); return true;
    }
    
    return false;
  }
  
  /**
   * Set the underlying channel.
   * @param channel The channel.
   */
  public synchronized void setChannel( IXioChannel channel)
  {
    this.channel = channel;
    bindProtocol.requestCompressor.setChannel( channel);
    bindProtocol.responseCompressor.setChannel( channel);
    getRemoteAddress();
  }
  
  /**
   * @return Returns null or the underlying channel.
   */
  public synchronized IXioChannel getChannel()
  {
    return channel;
  }
  
  /**
   * @return Returns the remote address to which this client is, or was last, connected.
   */
  public synchronized InetSocketAddress getLocalAddress()
  {
    return (channel != null)? (InetSocketAddress)channel.getLocalAddress(): null;
  }
  
  /**
   * @return Returns the remote address to which this client is, or was last, connected.
   */
  public synchronized InetSocketAddress getRemoteAddress()
  {
    return (channel != null)? (InetSocketAddress)channel.getRemoteAddress(): null;
  }
  
  /**
   * Close the connection.
   */
  public AsyncFuture<INetworkConnection> close()
  {
    return channel.close();
  }

  /**
   * Reset the peer after it has been disconnected.
   */
  public void reset()
  {
    headerProtocol.reset();
    bindProtocol.reset();
    executionProtocol.reset();
  }
  
  /**
   * @return Returns null or the peer registry.
   */
  public IXioPeerRegistry getPeerRegistry()
  {
    return registry;
  }

  /**
   * @return Returns the context used during connect, disconnect, register and unregister callbacks.
   */
  public StatefulContext getNetworkEventContext()
  {
    return eventContext;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object object)
  {
    IXioChannel otherChannel = ((XioPeer)object).getChannel();
    if ( channel == null || otherChannel == null) return false;
    return channel == otherChannel;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    if ( channel == null) return 0;
    return channel.hashCode();
  }

  /**
   * Dump the content of the specified buffer.
   * @param indent The indentation before each line.
   * @param buffer The buffer.
   * @return Returns a string containing the dump.
   */
  public final static String toString( String indent, ChannelBuffer buffer)
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "\n");
    
    for( int i=0, n=0; i<buffer.readableBytes(); i++, n++)
    {
      if ( i > 0 && (n % 64) == 0) sb.append( "\n");
      sb.append( String.format( "%02x", buffer.getByte( buffer.readerIndex() + i)));
    }
    
    sb.append( "\n");
    
//    sb.append( indent);
//    
//    int bpl = 64;
//    for( int i=0, n=0; i<buffer.readableBytes(); i++)
//    {
//      if ( n == 0)
//      {
//        for( int j=0; j<bpl && (i + j) < buffer.readableBytes(); j+=4)
//          sb.append( String.format( "|%-8d", i + j));
//        sb.append( String.format( "\n%s", indent));
//      }
//      
//      if ( (n % 4) == 0) sb.append( "|");
//      sb.append( String.format( "%02x", buffer.getByte( buffer.readerIndex() + i)));
//        
//      if ( ++n == bpl) 
//      { 
//        sb.append( String.format( "\n%s", indent));
//        n=0;
//      }
//    }
    
    return sb.toString();
  }

  private final static Log log = Log.getLog( XioPeer.class);
  
  private IXioChannel channel;
  private IXioPeerRegistry registry;
  private StatefulContext eventContext;
  protected HeaderProtocol headerProtocol;
  protected EchoProtocol echoProtocol;
  protected BindProtocol bindProtocol;
  protected ExecutionProtocol executionProtocol;
  protected RegisterProtocol registerProtocol;
}
