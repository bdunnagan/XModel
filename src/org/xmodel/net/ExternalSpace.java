/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ExternalSpace.java
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

import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.ModelRegistry;
import org.xmodel.external.CachingException;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalSpace;
import org.xmodel.external.UnboundedCache;


/**
 * An implementation of IExternalSpace for handling URIs representing queries to an XModel server.
 * URIs for this server have the "xmodel" scheme.  The host specification designates the location
 * of the server and the query contains the XPath with appropriate escaping per the URI specification.
 */
public class ExternalSpace implements IExternalSpace
{
  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalSpace#contains(java.net.URI)
   */
  public boolean contains( URI uri)
  {
    String scheme = uri.getScheme();
    return scheme.equals( "xmodel") || scheme.equals( "xml");
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalSpace#query(java.net.URI)
   */
  @SuppressWarnings("unchecked")
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
