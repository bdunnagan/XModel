package org.xmodel.caching.sql;

import org.xmodel.IModelObject;
import org.xmodel.external.AbstractCachingPolicy;
import org.xmodel.external.CachingException;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.ITransaction;
import org.xmodel.log.SLog;

/**
 * An implementation of ICachingPolicy for loading table rows.
 */
public class SQLRowCachingPolicy extends AbstractCachingPolicy
{
  protected SQLRowCachingPolicy( ICache cache)
  {
    super( cache);
  }
  
  /**
   * Set the SQLTableCachingPolicy.
   * @param parent The parent caching policy.
   */
  protected void setParent( SQLTableCachingPolicy parent)
  {
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
   * @see org.xmodel.external.ICachingPolicy#sync(org.xmodel.external.IExternalReference)
   */
  public void sync( IExternalReference reference) throws CachingException
  {
    SLog.debugf( this, "sync row: %s", reference.getID());
    
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

  /* (non-Javadoc)
   * @see org.xmodel.external.AbstractCachingPolicy#transaction()
   */
  @Override
  public ITransaction transaction()
  {
    return parent.transaction();
  }
  
  private SQLTableCachingPolicy parent;
}