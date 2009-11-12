/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * NetworkCachingPolicy.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.net;

import java.io.IOException;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.net.robust.ISession;
import org.xmodel.xpath.expression.IContext;


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
   * @see org.xmodel.external.ConfiguredCachingPolicy#configure(org.xmodel.IModelObject)
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
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(
   * org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    assert( this.reference != null && this.reference != reference);
    this.reference = reference;
    
    try
    {
      client = new ModelClient( host, port, getCache(), reference.getModel());
      client.addListener( listener);
      client.open();
    }
    catch( IOException e)
    {
      throw new CachingException( "Unable to connect to server: ", e);
    }
    
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
   * @see org.xmodel.external.AbstractCachingPolicy#clear(org.xmodel.external.IExternalReference)
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
   * @see org.xmodel.external.ICachingPolicy#flush(org.xmodel.external.IExternalReference)
   */
  public void flush( IExternalReference reference) throws CachingException
  {
    throw new CachingException( "Flush operation is not supported.");
  }
  
  private final ISession.IListener listener = new ISession.IListener() {
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
