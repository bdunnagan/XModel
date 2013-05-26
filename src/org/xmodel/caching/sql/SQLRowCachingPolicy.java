package org.xmodel.caching.sql;

import org.xmodel.IModelObject;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.ITransaction;
import org.xmodel.log.SLog;

/**
 * An implementation of ICachingPolicy for loading table rows.
 */
public class SQLRowCachingPolicy extends ConfiguredCachingPolicy
{
  public SQLRowCachingPolicy( ICache cache)
  {
    super( cache);
  }
  
  public SQLRowCachingPolicy( SQLTableCachingPolicy parent, ICache cache)
  {
    super( cache);
    this.parent = parent;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.AbstractCachingPolicy#addStaticAttribute(java.lang.String)
   */
  @Override
  public void addStaticAttribute( String attrName)
  {
    super.addStaticAttribute( attrName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  public void syncImpl( IExternalReference reference) throws CachingException
  {
    SLog.debugf( this, "sync row: %s", reference.getAttribute( "id"));

    SQLTableCachingPolicy parent = getParent( reference);
    parent.setUpdateMonitorEnabled( false);
    try
    {
      IModelObject object = parent.createRowPrototype( reference);
      update( reference, object);
    }
    finally
    {
      parent.setUpdateMonitorEnabled( true);
    }
  }
  
  /**
   * @param reference The row reference.
   * @return Returns the parent caching policy.
   */
  private SQLTableCachingPolicy getParent( IExternalReference reference)
  {
    if ( parent == null)
      parent = (SQLTableCachingPolicy)((IExternalReference)reference.getParent()).getCachingPolicy();
    return parent;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.AbstractCachingPolicy#transaction()
   */
  @Override
  public ITransaction transaction()
  {
    return parent.transaction();
  }
  
  protected SQLTableCachingPolicy parent;
}