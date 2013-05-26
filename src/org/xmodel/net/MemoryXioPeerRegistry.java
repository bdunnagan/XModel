package org.xmodel.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmodel.future.AsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.log.SLog;

public class MemoryXioPeerRegistry implements IXioPeerRegistry
{
  public MemoryXioPeerRegistry( XioServer server)
  {
    peersByName = new HashMap<String, Set<XioPeer>>();
    namesByPeer = new HashMap<XioPeer, Set<String>>();
    listeners = new ArrayList<IXioPeerRegistryListener>();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#register(org.xmodel.net.XioPeer, java.lang.String)
   */
  @Override
  public void register( XioPeer peer, String name)
  {
    IXioPeerRegistryListener[] array = null;
    
    synchronized( this)
    {
      Set<XioPeer> peers = peersByName.get( name);
      if ( peers == null)
      {
        peers = new HashSet<XioPeer>();
        peersByName.put( name, peers);
      }
      
      peers.add( peer);
      
      Set<String> names = namesByPeer.get( peer);
      if ( names == null)
      {
        names = new HashSet<String>();
        namesByPeer.put( peer, names);
      }
      
      names.add( name);
      
      array = listeners.toArray( new IXioPeerRegistryListener[ 0]);
    }
    
    for( IXioPeerRegistryListener listener: array)
      listener.onRegister( peer, name);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#unregister(org.xmodel.net.XioPeer, java.lang.String)
   */
  @Override
  public void unregister( XioPeer peer, String name)
  {
    IXioPeerRegistryListener[] array = null;
    
    synchronized( this)
    {
      Set<XioPeer> peers = peersByName.get( name);
      if ( peers != null) 
      {
        peers.remove( peer);
        if ( peers.size() == 0) peersByName.remove( name);
        
        Set<String> set = namesByPeer.get( peer);
        set.remove( name);
        if ( set.size() == 0) namesByPeer.remove( peer);
      }
      
      array = listeners.toArray( new IXioPeerRegistryListener[ 0]);
    }
    
    for( IXioPeerRegistryListener listener: array)
      listener.onUnregister( peer, name);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#unregisterAll(org.xmodel.net.XioPeer)
   */
  @Override
  public void unregisterAll( XioPeer peer)
  {
    List<String> names = new ArrayList<String>();
    
    synchronized( this)
    {
      Set<String> set = namesByPeer.get( peer);
      if ( set != null) names.addAll( set);
    }
    
    for( String name: names)
      unregister( peer, name);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#getRegistrationFuture(java.lang.String)
   */
  @Override
  public AsyncFuture<XioPeer> getRegistrationFuture( final String name)
  {
    class Listener implements IXioPeerRegistryListener
    {
      public Listener( AsyncFuture<XioPeer> future)
      {
        this.future = future;
      }
      
      public void onRegister( XioPeer peer, String peerName)
      {
        try
        {
          if ( name.equals( peerName))
            future.notifySuccess();
        }
        catch( Exception e)
        {
          SLog.exception( MemoryXioPeerRegistry.this, e);
        }
      }
      
      public void onUnregister( XioPeer peer, String peerName)
      {
      }
      
      private AsyncFuture<XioPeer> future;
    }

    class Future extends AsyncFuture<XioPeer>
    {
      public Future()
      {
        super( null);
      }
      
      public void cancel()
      {
        MemoryXioPeerRegistry.this.removeListener( listener);
      }
      
      protected Listener listener;
    }
    
    synchronized( this)
    {
      Set<XioPeer> peers = peersByName.get( name);
      if ( peers.size() > 0) return new SuccessAsyncFuture<XioPeer>( peers.iterator().next());
    
      Future future = new Future();
      Listener listener = new Listener( future);
      future.listener = listener;
      
      addListener( listener);
      
      return future;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#lookupByName(java.lang.String)
   */
  @Override
  public synchronized Iterator<XioPeer> lookupByName( String name)
  {
    Set<XioPeer> set = peersByName.get( name);
    if ( set == null) return Collections.<XioPeer>emptyList().iterator();
    
    List<XioPeer> peers = new ArrayList<XioPeer>( set);
    return peers.iterator();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#addListener(org.xmodel.net.IXioPeerRegistryListener)
   */
  @Override
  public synchronized void addListener( IXioPeerRegistryListener listener)
  {
    listeners.add( listener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IXioPeerRegistry#removeListener(org.xmodel.net.IXioPeerRegistryListener)
   */
  @Override
  public void removeListener( IXioPeerRegistryListener listener)
  {
    listeners.remove( listener);
  }

  private Map<String, Set<XioPeer>> peersByName;
  private Map<XioPeer, Set<String>> namesByPeer;
  private List<IXioPeerRegistryListener> listeners;
}
