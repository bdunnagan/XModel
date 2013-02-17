package org.xmodel.net;

import java.util.Iterator;
import org.jboss.netty.channel.Channel;

/**
 * Interface for registering and querying peer connections by name.
 */
public interface IXioPeerRegistry
{
  /**
   * Register the specified remote-host with the specified name, remote-server reverse connection
   * and port.  This registration provides all the information that a server needs to obtain a peer 
   * connection.
   * @param name A name, not necessarily unique, to associate with the peer.
   * @param host The host to be registered.
   * @param port The server port number for reverse connection.
   */
  public void register( String name, String host, int port);
  
  /**
   * Cancel a peer registration by name and host.
   * @param name The name associated with the peer.
   * @param host The remote host.
   */
  public void cancel( String name, String host);

  /**
   * Returns an iterator over XioPeer instances registered under the specified name.
   * @param name The name.
   * @return Returns the associated peers.
   */
  public Iterator<XioPeer> lookup( String name);
  
  /**
   * Called when a channel is connected.
   * @param channel The channel.
   */
  public void channelConnected( Channel channel);
  
  /**
   * Called when a channel is disconnected.
   * @param channel The channel.
   */
  public void channelDisconnected( Channel channel);
}
