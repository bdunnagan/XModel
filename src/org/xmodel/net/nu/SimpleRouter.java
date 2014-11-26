package org.xmodel.net.nu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SimpleRouter implements IRouter
{
  public SimpleRouter()
  {
    this.routes = new HashMap<String, Set<ITransport>>();
    this.routesLock = new ReentrantReadWriteLock();
  }
  
  @Override
  public void addRoute( String route, ITransport transport)
  {
    try
    {
      routesLock.writeLock().lock();
      Set<ITransport> transports = routes.get( route);
      if ( transports == null)
      {
        transports = new HashSet<ITransport>();
        routes.put( route, transports);
      }
      transports.add( transport);
    }
    finally
    {
      routesLock.writeLock().unlock();
    }
  }

  @Override
  public void removeRoute( String route, ITransport transport)
  {
    try
    {
      routesLock.writeLock().lock();
      Set<ITransport> transports = routes.get( route);
      if ( transports != null) transports.remove( transport);
      if ( transports.size() == 0) routes.remove( route);
    }
    finally
    {
      routesLock.writeLock().unlock();
    }
  }

  @Override
  public Iterator<String> removeRoutes( ITransport transport)
  {
    try
    {
      List<String> keys = findRoutes( transport);
      routesLock.writeLock().lock();
      for( String key: keys)
      {
        Set<ITransport> transports = routes.get( key);
        if ( transports != null) transports.remove( transport);
        if ( transports.size() == 0) routes.remove( key);
      }
      return keys.iterator();
    }
    finally
    {
      routesLock.writeLock().unlock();
    }
  }
  
  protected List<String> findRoutes( ITransport transport)
  {
    List<String> keys = new ArrayList<String>( 3);
    try
    {
      routesLock.readLock().lock();
      for( Map.Entry<String, Set<ITransport>> entry: routes.entrySet())
      {
        if ( entry.getValue().contains( transport))
          keys.add( entry.getKey());
      }
      return keys;
    }
    finally
    {
      routesLock.readLock().unlock();
    }
  }

  @Override
  public boolean hasRoute( String route)
  {
    try
    {
      routesLock.readLock().lock();
      return routes.containsKey( route);
    }
    finally
    {
      routesLock.readLock().unlock();
    }
  }

  @Override
  public Iterator<ITransport> resolve( String route)
  {
    try
    {
      routesLock.readLock().lock();
      Set<ITransport> transports = routes.get( route);
      if ( transports != null)
      {
        ArrayList<ITransport> copy = new ArrayList<ITransport>( transports);
        return copy.iterator();
      }
      else
      {
        return Collections.<ITransport>emptyList().iterator();
      }
    }
    finally
    {
      routesLock.readLock().unlock();
    }
  }
  
  private Map<String, Set<ITransport>> routes;
  private ReadWriteLock routesLock;
}
