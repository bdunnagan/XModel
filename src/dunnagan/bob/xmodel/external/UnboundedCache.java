/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external;

import dunnagan.bob.xmodel.IModelObject;

/**
 * An implementation of ICache which is unbounded (never clears its references).
 */
public class UnboundedCache implements ICache
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICache#configure(dunnagan.bob.xmodel.IModelObject)
   */
  public void configure( IModelObject annotation)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICache#add(dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void add( IExternalReference reference)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICache#remove(dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void remove( IExternalReference reference)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICache#touch(dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void touch( IExternalReference reference)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICache#size()
   */
  public int size()
  {
    return -1;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICache#capacity()
   */
  public int capacity()
  {
    return -1;
  }
}
