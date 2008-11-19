/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.external;

import org.xmodel.IModelObject;

/**
 * An implementation of ICache which is unbounded (never clears its references).
 */
public class UnboundedCache implements ICache
{
  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#configure(org.xmodel.IModelObject)
   */
  public void configure( IModelObject annotation)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#add(org.xmodel.external.IExternalReference)
   */
  public void add( IExternalReference reference)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#remove(org.xmodel.external.IExternalReference)
   */
  public void remove( IExternalReference reference)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#touch(org.xmodel.external.IExternalReference)
   */
  public void touch( IExternalReference reference)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#size()
   */
  public int size()
  {
    return -1;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICache#capacity()
   */
  public int capacity()
  {
    return -1;
  }
}
