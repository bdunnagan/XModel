/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net;

import java.net.InetSocketAddress;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.external.*;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

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
    this( cache, "", 0, null);
  }
  
  /**
   * Create a NetworkCachingPolicy which connects locally. 
   * @param cache The cache.
   * @param query The path of the root in the remote model.
   */
  public NetworkCachingPolicy( ICache cache, String query)
  {
    this( cache, ModelServer.defaultHost, ModelServer.defaultPort, query);
  }

  /**
   * Create a NetworkCachingPolicy which addresses the specified server.
   * @param cache The cache.
   * @param host The server host.
   * @param port The server port.
   * @param query The path of the root in the remote model.
   */
  public NetworkCachingPolicy( ICache cache, String host, int port, String query)
  {
    super( cache);
    setStaticAttributes( new String[] { "id", "xm:*", "net:*"});
    
    this.query = query;
    this.host = host;
    this.port = port;

    defineSecondaryStage( descendantExpr, this, false);
  }

  /**
   * Create a NetworkCachingPolicy with the specified client and query.
   * @param cache The cache.
   * @param client The client.
   * @param query The path of the root in the remote model.
   */
  public NetworkCachingPolicy( ICache cache, ModelClient client, String query)
  {
    super( cache);
    setStaticAttributes( new String[] { "id", "xm:*", "remote:*"});
    
    this.query = query;
    this.client = client;
    
    InetSocketAddress address = client.getRemoteAddress();
    this.host = address.getHostName();
    this.port = address.getPort();

    defineSecondaryStage( descendantExpr, this, false);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable
  {
    client.close();
    super.finalize();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ConfiguredCachingPolicy#configure(dunnagan.bob.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    // create client if necessary
    host = Xlate.childGet( annotation, "host", ModelServer.defaultHost);
    port = Xlate.childGet( annotation, "port", ModelServer.defaultPort);
    
    // set query if necessary
    if ( query == null) query = Xlate.childGet( annotation, "query", "");
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
    boolean isRoot = isRoot( reference);
    try
    {
      IModelObject result = null;
      if ( isRoot) 
      {
        if ( client == null)
        {
          // create and open new client session
          client = new ModelClient( host, port, reference.getModel());
          client.open();
        }
        
        // perform sync query
        List<IModelObject> elements = client.evaluateNodes( query);
        if ( elements.size() > 0) result = elements.get( 0);
      }
      else
      {
        // send sync request
        result = client.sendSyncRequest( reference);
      }

      // result may be null if the sync occured and the backing element has already been removed
      // in which case there is a pending delete for this reference and syncing is not necessary
      // FIXME: this behavior is slightly erroneous because it would appear that reference never
      // had any information, which is not true.
      if ( result == null) return;

      // register root
      if ( isRoot) client.register( Xlate.get( result, "remote:id", ""), reference);
      
      // update
      result.removeFromParent();
      update( reference, result);
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to connect to remote datamodel.", e);
    }
  }
  
  /**
   * Returns true if the specified reference is the root of the remote tree.
   * @param reference The reference to be tested.
   * @return Returns true if the specified reference is the root of the remote tree.
   */
  private boolean isRoot( IExternalReference reference)
  {
    IModelObject parent = reference.getParent();
    return parent == null || 
          !(parent instanceof IExternalReference) || 
          ((IExternalReference)parent).getCachingPolicy() != this;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.AbstractCachingPolicy#clear(dunnagan.bob.xmodel.external.IExternalReference)
   */
  @Override
  public void clear( IExternalReference reference) throws CachingException
  {
    if ( isRoot( reference))
    {
      // dispose of session
      if ( client != null)
      {
        client.close();
        client = null;
      }
    }
    else
    {
      super.clear( reference);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#flush(dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void flush( IExternalReference reference) throws CachingException
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#insert(
   * dunnagan.bob.xmodel.external.IExternalReference, dunnagan.bob.xmodel.IModelObject, boolean)
   */
  public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException
  {
    ExternalReference reference = new ExternalReference( object.getType());
    reference.setCachingPolicy( this);

    // diff
    differ.diffAndApply( reference, object);

    // register remote ids
    client.register( reference);
    
    // set dirty flags
    for( IModelObject partial: stubsExpr.query( reference, null))
      ((IExternalReference)partial).setDirty( true);
    reference.setDirty( Xlate.get( object, "remote:stub", false));
    
    // add to parent
    if ( index < 0) parent.addChild( reference); else parent.addChild( reference, index);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ConfiguredCachingPolicy#update(dunnagan.bob.xmodel.external.IExternalReference, dunnagan.bob.xmodel.IModelObject)
   */
  @Override
  public void update( IExternalReference reference, IModelObject object) throws CachingException
  {
    reference.getModel().setSyncLock( true);
    try
    {
      super.update( reference, object);
      
      // register remote ids
      client.register( reference);
      
      // mark dirty
      for( IModelObject partial: stubsExpr.query( reference, null))
        ((IExternalReference)partial).setDirty( true);
    }
    finally
    {
      reference.getModel().setSyncLock( false);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#remove(dunnagan.bob.xmodel.external.IExternalReference, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
    object.removeFromParent();
  }

  private static IExpression descendantExpr = XPath.createExpression( "nosync( descendant::*)");
  private static IExpression stubsExpr = XPath.createExpression( "nosync( descendant::*[ @remote:stub = 'true'])");
  
  private String host;
  private int port;
  private ModelClient client;
  private String query;
}
