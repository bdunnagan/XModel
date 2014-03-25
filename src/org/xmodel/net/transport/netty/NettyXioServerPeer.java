package org.xmodel.net.transport.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.net.XioChannel;
import org.xmodel.net.IXioPeerRegistry;
import org.xmodel.net.XioPeer;
import org.xmodel.net.protocol.execute.ExecutionPrivilege;
import org.xmodel.xpath.expression.IContext;

public class NettyXioServerPeer extends XioPeer
{
  public NettyXioServerPeer( 
      XioChannel channel, 
      IXioPeerRegistry registry, 
      IContext context, 
      Executor executor, 
      ScheduledExecutorService scheduler,
      ExecutionPrivilege executionPrivilege)
  {
    super( channel, registry, context, executor, scheduler, executionPrivilege);
  }

  /**
   * @return Returns the remote address to which this client is, or was last, connected.
   */
  public synchronized InetSocketAddress getRemoteAddress()
  {
    if ( address != null) return address;
    XioChannel channel = getChannel();
    address = (channel != null)? (InetSocketAddress)channel.getRemoteAddress(): null;
    return address;
  }
  
  /**
   * Set the registered peer of this peer.
   * @param name The name.
   */
  public synchronized void setRegisteredName( String name)
  {
    this.name = name;
  }
  
  /**
   * @return Returns null or the registered name of this peer.
   */
  public synchronized String getRegisteredName()
  {
    return name;
  }

  private InetSocketAddress address;
  private String name;
}
