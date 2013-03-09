package org.xmodel.net;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.netty.channel.Channel;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.log.SLog;

public class MemoryXioPeerRegistry implements IXioPeerRegistry
{
  public MemoryXioPeerRegistry( XioServer server)
  {
    this.server = server;
    this.channelsByHost = new ConcurrentHashMap<String, Channel>();
    this.hostsByName = new HashMap<String, List<String>>();
    this.futuresByHost = new HashMap<String, AsyncFuture<XioPeer>>();
    this.listeners = Collections.synchronizedList( new ArrayList<IXioPeerRegistryListener>( 1));
    this.lookupByHostLock = new Object();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#register(java.lang.String, java.lang.String)
   */
  @Override
  public void register( String name, String host)
  {
    synchronized( this)
    {
      List<String> hosts = getHostsByName( name);
      hosts.add( host);
    }
    
    // notify listeners
    for( int i=0; i<listeners.size(); i++)
      listeners.get( i).onRegister( name, host);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#cancel(java.lang.String, java.lang.String)
   */
  @Override
  public void unregister( String name, String host)
  {
    synchronized( this)
    {
      hostsByName.remove( name);
    }
    
    // notify listeners
    for( int i=0; i<listeners.size(); i++)
      listeners.get( i).onUnregister( name, host);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#lookupByHost(java.lang.String)
   */
  @Override
  public AsyncFuture<XioPeer> lookupByHost( final String host)
  {
    synchronized( lookupByHostLock)
    {
      Channel channel = channelsByHost.get( host);
      if ( channel != null) return new SuccessAsyncFuture<XioPeer>( new XioServerPeer( server, host, channel));
      
      AsyncFuture<XioPeer> future = futuresByHost.get( host);
      if ( future == null)
      {
        future = new AsyncFuture<XioPeer>( new XioServerPeer( server, host, null)) {
          public void cancel()
          {
            futuresByHost.remove( host);
          }
        };
        
        futuresByHost.put( host, future);
      }
      
      return future;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#lookup(java.lang.String)
   */
  @Override
  public synchronized Iterator<XioPeer> lookupByName( String name)
  {
    List<XioPeer> peers = new ArrayList<XioPeer>();
    
    List<String> hosts = getHostsByName( name);
    for( String host: hosts)
    {
      Channel channel = channelsByHost.get( host);
      peers.add( new XioServerPeer( server, host, channel));
    }
    
    return peers.iterator();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#addListener(org.xmodel.net.IXioPeerRegistry.IListener)
   */
  @Override
  public void addListener( IXioPeerRegistryListener listener)
  {
    if ( !listeners.contains( listener))
      listeners.add( listener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#removeListener(org.xmodel.net.IXioPeerRegistry.IListener)
   */
  @Override
  public void removeListener( IXioPeerRegistryListener listener)
  {
    listeners.remove( listener);
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
      
      // complete pending future
      AsyncFuture<XioPeer> future = null;
      synchronized( lookupByHostLock)
      {
        String host = ((InetSocketAddress)channel.getRemoteAddress()).getHostName();
        future = futuresByHost.get( host);
      }
      
      if ( future != null) future.notifySuccess();
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
   * Returns the hosts associated with the specified name.
   * @param name The name.
   * @return Returns the host list.
   */
  private List<String> getHostsByName( String name)
  {
    List<String> hosts = hostsByName.get( name);
    if ( hosts == null)
    {
      hosts = new ArrayList<String>();
      hostsByName.put( name, hosts);
    }
    return hosts;
  }  
  
  private XioServer server;
  private Map<String, Channel> channelsByHost;
  private Map<String, List<String>> hostsByName;
  private List<IXioPeerRegistryListener> listeners;
  private Map<String, AsyncFuture<XioPeer>> futuresByHost;
  private Object lookupByHostLock;
}
