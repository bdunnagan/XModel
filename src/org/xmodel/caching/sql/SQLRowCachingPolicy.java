package org.xmodel.caching.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.ITransaction;
import org.xmodel.log.SLog;

/**
 * An ICachingPolicy used for the second layer of caching when the SQLCachingPolicy is configured
 * with <i>update</i> evaluating true.  
 */
class SQLRowCachingPolicy extends ConfiguredCachingPolicy
{
  public SQLRowCachingPolicy( ICache cache)
  {
    super( cache);
  }
  
  public SQLRowCachingPolicy( SQLCachingPolicy parent, ICache cache)
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
    SLog.debugf( this, "sync row: %s", reference.getID());

    SQLCachingPolicy parent = getParent( reference);
    parent.updateListener.setEnabled( false);
    try
    {
      IModelObject object = parent.createRowPrototype( reference);
      update( reference, object);
    }
    finally
    {
      parent.updateListener.setEnabled( true);
    }
  }
  
  /**
   * @param reference The row reference.
   * @return Returns the parent caching policy.
   */
  private SQLCachingPolicy getParent( IExternalReference reference)
  {
    if ( parent == null)
      parent = (SQLCachingPolicy)((IExternalReference)reference.getParent()).getCachingPolicy();
    return parent;
  }

  /**
   * Create the row element corresponding to the specified unsynced referenced.
   * @param reference The reference which is in the process of being synced.
   * @return Returns the prototype row element.
   */
  protected IModelObject createRowPrototype( IExternalReference reference) throws CachingException
  {
    try
    {
      IModelObject object = getFactory().createObject( reference.getParent(), reference.getType());
      ModelAlgorithms.copyAttributes( reference, object);

      PreparedStatement statement = createRowSelectStatement( reference);
      ResultSet result = statement.executeQuery();
      if ( result.next()) populateRowElement( result, object);
      
      statement.close();

      return object;
    }
    catch( SQLException e)
    {
      throw new CachingException( "Unable to cache reference: "+reference, e);
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
  
  protected SQLCachingPolicy parent;
}