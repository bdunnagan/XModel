/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net;

import java.net.URI;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelAlgorithms;
import dunnagan.bob.xmodel.ModelRegistry;
import dunnagan.bob.xmodel.external.CachingException;
import dunnagan.bob.xmodel.external.ExternalReference;
import dunnagan.bob.xmodel.external.IExternalSpace;
import dunnagan.bob.xmodel.external.UnboundedCache;

/**
 * An implementation of IExternalSpace for handling URIs representing queries to an XModel server.
 * URIs for this server have the "xmodel" scheme.  The host specification designates the location
 * of the server and the query contains the XPath with appropriate escaping per the URI specification.
 */
public class ExternalSpace implements IExternalSpace
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.IExternalSpace#contains(java.net.URI)
   */
  public boolean contains( URI uri)
  {
    String scheme = uri.getScheme();
    return scheme.equals( "xmodel") || scheme.equals( "xml");
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.IExternalSpace#query(java.net.URI)
   */
  public IModelObject query( URI uri) throws CachingException
  {
    if ( !contains( uri)) throw new CachingException( "Invalid URI scheme: "+uri.getScheme());
    
    String host = uri.getHost();
    int port = uri.getPort();
    String query = uri.getQuery();
    
    try
    {
      ModelClient client = new ModelClient( host, port, ModelRegistry.getInstance().getModel());
      client.open();
      
      NetworkCachingPolicy cachingPolicy = new NetworkCachingPolicy( new UnboundedCache(), client, query);
      List<IModelObject> nodes = client.evaluateNodes( query);
      if ( nodes.size() > 0)
      {
        IModelObject node = nodes.get( 0);
        ExternalReference reference = new ExternalReference( node.getType());
        reference.setCachingPolicy( cachingPolicy, false);
        ModelAlgorithms.copyAttributes( node, reference);
        cachingPolicy.update( reference, node);
        return reference;
      }
      
      return null;
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to perform query: "+uri, e); 
    }
  }
}
