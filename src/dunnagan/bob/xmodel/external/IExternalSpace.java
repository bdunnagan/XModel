/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external;

import java.net.URI;

import dunnagan.bob.xmodel.IModelObject;

/**
 * An interface for creating a data-model to represent a URI specification for a particular URI scheme.
 * This interface is used by the <code>fn:doc</code> function to resolve a URI.
 */
public interface IExternalSpace
{
  /**
   * Returns true if this space serves the scheme of the specified URI.
   * @param uri The URI.
   * @return Returns true if this space serves the scheme of the specified URI.
   */
  public boolean contains( URI uri);
  
  /**
   * Obtain the first element identified by the specified URI query.
   * @param uri The URI specification.
   * @return Returns the first element identitifed by the URI query.
   */
  public IModelObject query( URI uri) throws CachingException;
}
