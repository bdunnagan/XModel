package org.xmodel.net;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jboss.netty.channel.Channel;
import org.xmodel.log.SLog;

public class MemoryXioPeerRegistry implements IXioPeerRegistry
{
  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#register(java.lang.String, java.lang.String, int, long)
   */
  @Override
  public synchronized void register( String name, String host, int port)
  {
    Map<String, InetSocketAddress> addresses = getAddressesByName( name);
    addresses.put( host, new InetSocketAddress( host, port));
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#cancel(java.lang.String, java.lang.String)
   */
  @Override
  public synchronized void cancel( String name, String host)
  {
    Map<String, InetSocketAddress> addresses = getAddressesByName( name);
    addresses.remove( host);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#lookup(java.lang.String)
   */
  @Override
  public synchronized Iterator<XioPeer> lookup( String name)
  {
    List<XioPeer> peers = new ArrayList<XioPeer>();
    
    Map<String, InetSocketAddress> addresses = getAddressesByName( name);
    for( InetSocketAddress address: addresses.values())
    {
      String host = address.getHostName();
      Channel channel = channelsByHost.get( host);
      peers.add( new XioPeer( channel));
    }
    
    return peers.iterator();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#channelConnected(org.jboss.netty.channel.Channel)
   */
  @Override
  public void channelConnected( Channel channel)
  {
    try
    {
      InetSocketAddress address = (InetSocketAddress)channel.getRemoteAddress();
      channelsByHost.put( address.getHostName(), channel);
    }
    catch( Exception e)
    {
      SLog.exception( this, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#channelDisconnected(org.jboss.netty.channel.Channel)
   */
  @Override
  public void channelDisconnected( Channel channel)
  {
    try
    {
      InetSocketAddress address = (InetSocketAddress)channel.getRemoteAddress();
      channelsByHost.remove( address.getHostName());
    }
    catch( Exception e)
    {
      SLog.exception( this, e);
    }
  }
  
  /**
   * Returns the addresses associated with the specified name as a map of addresses by host.
   * @param name The name.
   * @return Returns a map containing all the associated addresses.
   */
  private Map<String, InetSocketAddress> getAddressesByName( String name)
  {
    Map<String, InetSocketAddress> addresses = addressesByName.get( name);
    if ( addresses == null)
    {
      addresses = new HashMap<String, InetSocketAddress>();
      addressesByName.put( name, addresses);
    }
    return addresses;
  }
  
  private Map<String, Channel> channelsByHost;
  private Map<String, Map<String, InetSocketAddress>> addressesByName;
}
