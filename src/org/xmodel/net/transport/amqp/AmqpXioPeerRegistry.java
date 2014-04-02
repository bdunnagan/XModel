package org.xmodel.net.transport.amqp;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xmodel.future.AsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.net.IXioPeerRegistry;
import org.xmodel.net.IXioPeerRegistryListener;
import org.xmodel.net.XioPeer;

public class AmqpXioPeerRegistry implements IXioPeerRegistry
{
  public AmqpXioPeerRegistry( IXioPeerRegistry backingRegistry)
  {
    this.backingRegistry = backingRegistry;
    this.derivedPeers = new ConcurrentHashMap<String, XioPeer>();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#register(org.xmodel.net.XioPeer, java.lang.String)
   */
  @Override
  public void register( XioPeer peer, String name)
  {
    String regName = AmqpQualifiedNames.parseRegistrationName( name);
    log.debugf( "Register peer: qname='%s', name='%s'", name, regName);
    
    Iterator<XioPeer> iterator = backingRegistry.lookupByName( regName);
    while( iterator.hasNext())
    {
      AmqpXioPeer oldPeer = (AmqpXioPeer)iterator.next();
      iterator.remove();
      oldPeer.close();
      log.debugf( "Closing previously registered peer: %s", oldPeer);
    }
    
    try
    {
      //
      // A new peer instance must be created here to communicate with the request/response queues
      // specific to the registration name.  Unlike in the Netty implementation, the peer passed
      // to this function is not unique to the remote endpoint.
      //
      peer = ((AmqpXioPeer)peer).deriveRegisteredPeer( name);
      log.debugf( "Creating new peer for remote: %X", peer.hashCode());
      derivedPeers.put( name, peer);
      
      // register
      backingRegistry.register( peer, regName);
    }
    catch( IOException e)
    {
      throw new IllegalStateException( String.format( "Caught exception trying to configure registered name channel, %s", name), e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#unregister(org.xmodel.net.XioPeer, java.lang.String)
   */
  @Override
  public void unregister( XioPeer peer, String name)
  {
    String regName = AmqpQualifiedNames.parseRegistrationName( name);
    log.debugf( "Unregister peer: qname='%s', name='%s'", name, regName);
    
    peer = derivedPeers.remove( name);
    if ( peer != null) backingRegistry.unregister( peer, regName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#unregisterAll(org.xmodel.net.XioPeer)
   */
  @Override
  public void unregisterAll( XioPeer peer)
  {
    // TODO: the protocol does not support identifying the correct peer when this message
    // is sent by the client.  However, when this method is called by the Heartbeat class
    // the Heartbeat class provides the correct peer instance.
    
    backingRegistry.unregisterAll( peer);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#getRegistrationFuture(java.lang.String)
   */
  @Override
  public AsyncFuture<XioPeer> getRegistrationFuture( String name)
  {
    return backingRegistry.getRegistrationFuture( name);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#lookupByName(java.lang.String)
   */
  @Override
  public Iterator<XioPeer> lookupByName( String name)
  {
    return backingRegistry.lookupByName( name);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#addListener(org.xmodel.net.IXioPeerRegistryListener)
   */
  @Override
  public void addListener( IXioPeerRegistryListener listener)
  {
    backingRegistry.addListener( listener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#removeListener(org.xmodel.net.IXioPeerRegistryListener)
   */
  @Override
  public void removeListener( IXioPeerRegistryListener listener)
  {
    backingRegistry.removeListener( listener);
  }

  public final static Log log = Log.getLog( AmqpXioPeerRegistry.class);
  
  private IXioPeerRegistry backingRegistry;
  private Map<String, XioPeer> derivedPeers;
}