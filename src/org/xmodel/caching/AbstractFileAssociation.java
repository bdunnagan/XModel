package org.xmodel.caching;

import java.io.InputStream;
import org.xmodel.INode;
import org.xmodel.external.CachingException;
import org.xmodel.external.ICachingPolicy;

/**
 * Abstract base implementation of IFileAssociation.  One of the two methods, <code>getCachingPolicy</code> or
 * <code>apply</code> must be implemented.
 */
public abstract class AbstractFileAssociation implements IFileAssociation
{
  /* (non-Javadoc)
   * @see org.xmodel.caching.IFileAssociation#getCachingPolicy(org.xmodel.external.ICachingPolicy, java.lang.String)
   */
  @Override
  public ICachingPolicy getCachingPolicy( ICachingPolicy parent, String name) throws CachingException
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.caching.IFileAssociation#apply(org.xmodel.IModelObject, java.lang.String, java.io.InputStream)
   */
  @Override
  public void apply( INode parent, String name, InputStream stream) throws CachingException
  {
  }
}
