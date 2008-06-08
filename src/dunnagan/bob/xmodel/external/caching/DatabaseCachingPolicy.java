/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external.caching;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.external.AbstractCachingPolicy;
import dunnagan.bob.xmodel.external.CachingException;
import dunnagan.bob.xmodel.external.EntityDiffer;
import dunnagan.bob.xmodel.external.ExternalReference;
import dunnagan.bob.xmodel.external.IExternalReference;

/**
 * The base class for caching policies which interface with a relational database. This caching policy is responsible for 
 * creating tables for any of its children which are instances of IExternalReference.  It is also responsible for updating
 * the xml in the database associated with the IExternalReference to which it belongs.  The xml which is stored in the
 * database is the document representing the subtree rooted at the IExternalReference with all of its secondary stages
 * removed.
 */
public abstract class DatabaseCachingPolicy extends AbstractCachingPolicy
{
  protected DatabaseCachingPolicy( String database)
  {
    this.database = database;
    differ = new EntityDiffer();
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#sync(dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void sync( IExternalReference reference) throws CachingException
  {
    try
    {
      IModelObject object = loadDatabaseEntry( reference);      
      differ.diffAndApply( reference, object);
    } 
    catch( Exception e)
    {
      throw new CachingException( "Unable to load database entry for reference: "+reference, e);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#flush(dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void flush( IExternalReference reference) throws CachingException
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#insert(dunnagan.bob.xmodel.external.IExternalReference, dunnagan.bob.xmodel.IModelObject, boolean)
   */
  public void insert( IExternalReference parent, IModelObject object, boolean dirty) throws CachingException
  {
    // create a table in the database if necessary
    try
    {
      createTable( object);
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to create table for references of type: "+object.getType(), e);
    }
    
    // create external reference
    IExternalReference reference = new ExternalReference( object.getType());
    differ.diffAndApply( reference, object);
    reference.setCachingPolicy( this, dirty);
    
    // update database
    try
    {
      createDatabaseEntry( reference);
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to update database entry for reference: "+object, e);
    }
    
    // add new reference to model
    parent.addChild( reference);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#update(dunnagan.bob.xmodel.external.IExternalReference, dunnagan.bob.xmodel.IModelObject)
   */
  public void update( IExternalReference reference, IModelObject object) throws CachingException
  {
    // updating the database with the object before culling information that overruns the boundaries
    // of secondary stages could lead to database bloat.  However, this is faster and easier.
    try
    {
      updateDatabaseEntry( object);
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to update database for reference: "+reference, e);
    }
    
    // update reference
    reference.setDirty( false);
    differ.diffAndApply( reference, object);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#remove(dunnagan.bob.xmodel.external.IExternalReference, dunnagan.bob.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
    IModelObject child = parent.getChild( object.getType(), object.getID());
    if ( child == null) throw new CachingException( "Reference not found: "+object);
    
    try
    {
      deleteDatabaseEntry( child);
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to remove database entry for reference: "+child, e);
    }
    
    child.removeFromParent();
  }
  
  /**
   * Returns the name of the database for this caching policy.
   * @return Returns the name of the database for this caching policy.
   */
  public String getDatabase()
  {
    return database;
  }
  
  /**
   * Returns the EntityDiffer responsible for updates to an IExternalReference subtree.
   * @return Returns the EntityDiffer responsible for updates to an IExternalReference subtree.
   */
  protected EntityDiffer getEntityDiffer()
  {
    return differ;
  }
  
  /**
   * Called to create a table in the database to hold the specified object.  This method is called each
   * time an object is inserted so the implementation must provide a fast method of determining if the
   * table needs to be created.
   * @param object The object to be stored.
   */
  abstract protected void createTable( IModelObject object) throws Exception;
  
  /**
   * Create the database entry for the specified object.
   * @param object The object.
   */
  abstract protected void createDatabaseEntry( IModelObject object) throws Exception;

  /**
   * Update the database entry for the specified object.
   * @param object The object.
   */
  abstract protected void updateDatabaseEntry( IModelObject object) throws Exception;

  /**
   * Delete the database entry for the specified object.
   * @param object The object.
   */
  abstract protected void deleteDatabaseEntry( IModelObject object) throws Exception;
  
  /**
   * Load the database entry for the specified reference.
   * @param reference The reference.
   * @return Returns the subtree retrieved from the database.
   */
  abstract protected IModelObject loadDatabaseEntry( IExternalReference reference) throws Exception;
  
  private String database;
  private EntityDiffer differ;
}
