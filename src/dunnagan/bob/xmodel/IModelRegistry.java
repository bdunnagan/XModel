/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import java.net.URI;
import java.util.List;

import dunnagan.bob.xmodel.external.IExternalSpace;

/**
 * An interface for adding, removing and accessing documents of collections and for accessing the
 * IModel associated with the current thread.  Each thread which manages an xmodel has its own
 * instance of IModel.
 */
public interface IModelRegistry
{
  /**
   * Returns the IModel associated with the current thread.
   * @return Returns the IModel associated with the current thread.
   */
  public IModel getModel();
  
  /**
   * Create a collection with the specified name and root element.
   * @param name The name of the collection and the root element.
   * @return Returns the root of the collection.
   */
  public IModelObject createCollection( String name);
  
  /**
   * Register an IExternalSpace for resolving URI queries.
   * @param externalSpace The external space.
   */
  public void register( IExternalSpace externalSpace);

  /**
   * Remove the specified IExternalSpace from the registry.
   * @param externalSpace The external space.
   */
  public void unregister( IExternalSpace externalSpace);
  
  /**
   * Query the specified URI specification and return the matching element. This method calls
   * the <code>contains</code> method of each registered IExternalSpace implementation and uses
   * the first match to resolve the query.
   * @param uri The URI specification.
   * @return Returns the result of the query.
   */
  public List<IModelObject> query( URI uri);
}
