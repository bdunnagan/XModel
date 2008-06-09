/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelRegistry;
import dunnagan.bob.xmodel.external.CachingException;
import dunnagan.bob.xmodel.external.ICache;
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
  public List<IModelObject> query( URI uri) throws CachingException
  {
    if ( !contains( uri)) throw new CachingException( "Invalid URI scheme: "+uri.getScheme());
    
    String host = uri.getHost();
    int port = uri.getPort();
    String query = uri.getQuery();
    
    try
    {
      ICache cache = new UnboundedCache();
      ModelClient client = new ModelClient( host, port, cache, ModelRegistry.getInstance().getModel());
      client.open();
      Object result = client.bind( query);
      if ( result instanceof List)
      {
        return (List<IModelObject>)result;
      }
      else
      {
        return Collections.emptyList();
      }
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to perform query: "+uri, e); 
    }
  }
  
  public final static int timeout = 30000;
}
