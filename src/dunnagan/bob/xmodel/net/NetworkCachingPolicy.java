/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net;

import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.external.CachingException;
import dunnagan.bob.xmodel.external.ConfiguredCachingPolicy;
import dunnagan.bob.xmodel.external.ICache;
import dunnagan.bob.xmodel.external.IExternalReference;
import dunnagan.bob.xmodel.net.robust.ISession;
import dunnagan.bob.xmodel.xpath.expression.IContext;

/**
 * A ConfiguredCachingPolicy which uses the simple client/server protocol defined in this
 * package to resolve external references.
 */
public class NetworkCachingPolicy extends ConfiguredCachingPolicy
{
  /**
   * Create a NetworkCachingPolicy which will be configured by annotation.
   * @param cache The cache.
   */
  public NetworkCachingPolicy( ICache cache)
  {
    super( cache);
    setStaticAttributes( new String[] { "id", "xm:*", "net:*"});
    this.host = ModelServer.defaultHost;
    this.port = ModelServer.defaultPort;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable
  {
    if ( client != null) client.close();
    super.finalize();
  }
  
  /**
   * Set the query.
   * @param query The query.
   */
  public void setQuery( String query)
  {
    this.query = query;
  }

  /**
   * Set the host.
   * @param host The host.
   */
  public void setHost( String host)
  {
    this.host = host;
  }
  
  /**
   * Set the port.
   * @param port The port.
   */
  public void setPort( int port)
  {
    this.port = port;
  }

  /**
   * Set the query limit.
   * @param limit The limit.
   */
  public void setQueryLimit( int limit)
  {
    this.limit = limit;
  }
  
  /**
   * By default, the first node in the query result will be used to update the reference. If the multi
   * flag is true, however, all the nodes in the query result will become children of the reference.
   * @param multi True if the nodes of the query result should become children of the reference.
   */
  public void setMultipleResult( boolean multi)
  {
    this.multi = multi;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ConfiguredCachingPolicy#configure(dunnagan.bob.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    multi = Xlate.childGet( annotation, "multi", false);
    host = Xlate.childGet( annotation, "host", ModelServer.defaultHost);
    port = Xlate.childGet( annotation, "port", ModelServer.defaultPort);
    if ( query == null) query = Xlate.childGet( annotation, "query", "");
    if ( limit == 0) limit = Xlate.childGet( annotation, "limit", 1000);
  }

  /**
   * Returns the client session if it has been created.
   * @return Returns null or the client session if it has been created.
   */
  public ModelClient getClient()
  {
    return client;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ConfiguredCachingPolicy#syncImpl(
   * dunnagan.bob.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    assert( this.reference != null && this.reference != reference);
    this.reference = reference;
    
    client = new ModelClient( host, port, getCache(), reference.getModel());
    client.addListener( listener);
    client.open();
    client.setQueryLimit( limit);
    try
    {
      if ( multi)
      {
        List<IModelObject> elements = client.bind( query);
        for( IModelObject element: elements) insert( reference, element, -1, false);
      }
      else
      {
        List<IModelObject> elements = client.bind( query);
        update( reference, elements.get( 0));
      }
    }
    catch( TimeoutException e)
    {
      throw new CachingException( "Timeout trying to sync network reference: "+reference, e);
    }
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.AbstractCachingPolicy#clear(dunnagan.bob.xmodel.external.IExternalReference)
   */
  @Override
  public void clear( IExternalReference reference) throws CachingException
  {
    try
    {
      if ( client != null) client.close();
      client = null;
    }
    finally
    {
      super.clear( reference);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#flush(dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void flush( IExternalReference reference) throws CachingException
  {
    throw new CachingException( "Flush operation is not supported.");
  }
  
  private final ISession.Listener listener = new ISession.Listener() {
    public void notifyOpen( ISession session)
    {
      AsyncRunnable runnable = new AsyncRunnable();
      runnable.session = session;
      runnable.state = "open";
      reference.getModel().dispatch( runnable);
    }
    public void notifyClose( ISession session)
    {
      AsyncRunnable runnable = new AsyncRunnable();
      runnable.session = session;
      runnable.state = "closed";
      reference.getModel().dispatch( runnable);
    }
    public void notifyConnect( ISession session)
    {
      AsyncRunnable runnable = new AsyncRunnable();
      runnable.session = session;
      runnable.state = "connected";
      reference.getModel().dispatch( runnable);
    }
    public void notifyDisconnect( ISession session)
    {
      AsyncRunnable runnable = new AsyncRunnable();
      runnable.session = session;
      runnable.state = "disconnected";
      reference.getModel().dispatch( runnable);
    }
  };
  
  private class AsyncRunnable implements Runnable
  {
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      reference.setAttribute( "net:state", state);
      reference.setAttribute( "net:session", session.getShortSessionID());
    }
    
    public ISession session;
    public String state;
  }

  private String host;
  private int port;
  private ModelClient client;
  private String query;
  private int limit;
  private boolean multi;
  private IExternalReference reference;
}
