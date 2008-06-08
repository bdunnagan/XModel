/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external;

import dunnagan.bob.xmodel.IModel;
import dunnagan.bob.xmodel.xml.IXmlIO;
import dunnagan.bob.xmodel.xml.XmlException;
import dunnagan.bob.xmodel.xml.XmlIO;

/**
 * A base implementation of ICachingPolicy which handles all the semantics except 
 * those pertaining to synchronizing references with their backing store.
 */
public abstract class AbstractCachingPolicy implements ICachingPolicy
{
  /**
   * Create an AbstractCachingPolicy which keeps everything in memory.
   */
  protected AbstractCachingPolicy()
  {
    this( new UnboundedCache());
  }
  
  /**
   * Create an AbstractCachingPolicy which uses the specified cache.
   * @param cache The cache.
   */
  protected AbstractCachingPolicy( ICache cache)
  {
    this.cache = cache;
    xmlIO = new XmlIO();
    staticAttributes = new String[] { "id"};
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#getCache()
   */
  public ICache getCache()
  {
    return cache;
  }

  /**
   * This default implementation does not performing locking.
   */
  public void checkin( IExternalReference reference)
  {
  }

  /**
   * This default implementation does not performing locking.
   */
  public void checkout( IExternalReference reference)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#prepare(dunnagan.bob.xmodel.external.IExternalReference, boolean)
   */
  public void prepare( IExternalReference reference, boolean dirty)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#clear(dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void clear( IExternalReference reference) throws CachingException
  {
    if ( reference.isDirty()) return;
    
    boolean resync = reference.hasListeners();
    reference.removeChildren();
    reference.setDirty( true);
    
    // must call reference.sync() here instead of calling sync( reference) because the former
    // sets the dirty flag to false before calling sync( reference)
    if ( resync) reference.sync();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#flush(dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void flush( IExternalReference reference) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#insert(dunnagan.bob.xmodel.external.IExternalReference, 
   * java.lang.String, boolean)
   */
  public void insert( IExternalReference parent, String xml, int index, boolean dirty) throws CachingException
  {
    try
    {
      insert( parent, xmlIO.read( xml), -1, dirty);
    }
    catch( XmlException e)
    {
      throw new CachingException( "Unable to insert entity: "+xml, e);
    } 
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#remove(dunnagan.bob.xmodel.external.IExternalReference, 
   * java.lang.String)
   */
  public void remove( IExternalReference parent, String xml) throws CachingException
  {
    try
    {
      remove( parent, xmlIO.read( xml));
    }
    catch( XmlException e)
    {
      throw new CachingException( "Unable to remove entity: "+xml, e);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#update(dunnagan.bob.xmodel.external.IExternalReference, 
   * java.lang.String)
   */
  public void update( IExternalReference reference, String xml) throws CachingException
  {
    try
    {
      update( reference, xmlIO.read( xml));
    }
    catch( XmlException e)
    {
      throw new CachingException( "Unable to update entity: "+xml, e);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#getStaticAttributes()
   */
  public String[] getStaticAttributes()
  {
    return staticAttributes;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#readAttributeAccess(
   * dunnagan.bob.xmodel.external.IExternalReference, java.lang.String)
   */
  public void readAttributeAccess( IExternalReference reference, String attrName)
  {
    if ( !isStaticAttribute( attrName)) 
    {
      if ( cache != null) cache.touch( reference);
      if ( reference.isDirty()) internal_sync( reference);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#readChildrenAccess(
   * dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void readChildrenAccess( IExternalReference reference)
  {
    if ( cache != null) cache.touch( reference);
    if ( reference.isDirty()) internal_sync( reference);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#writeAttributeAccess(
   * dunnagan.bob.xmodel.external.IExternalReference, java.lang.String)
   */
  public void writeAttributeAccess( IExternalReference reference, String attrName)
  {
    if ( !isStaticAttribute( attrName)) 
    {
      if ( cache != null) cache.touch( reference);
      if ( reference.isDirty()) internal_sync( reference);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#writeChildrenAccess(
   * dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void writeChildrenAccess( IExternalReference reference)
  {
    if ( cache != null) cache.touch( reference);
    if ( reference.isDirty()) internal_sync( reference);
  }
  
  /**
   * Specify the names of attributes which should not cause synchronization. Two types of 
   * wildcards can be used. An asterisk by itself means <i>all attributes</i>. A prefix 
   * ending with a colon followed by an asterisk means <i>all attributes in namespace</i>.
   * @param staticAttributes An array of attribute names.
   */
  protected void setStaticAttributes( String[] staticAttributes)
  {
    this.staticAttributes = staticAttributes;
  }
  
  /**
   * Returns true if the specified attribute is in the list of static attributes.
   * @param attribute The attribute to be tested.
   * @return Returns true if the specified attribute is in the list of static attributes.
   */
  public boolean isStaticAttribute( String attribute)
  {
    if ( attribute == null)
    {
      return staticAttributes.length > 0 && staticAttributes[ 0].equals( "*");
    }
    else
    {
      for( int i=0; i<staticAttributes.length; i++)
      {
        String staticAttribute = staticAttributes[ i];
        if ( staticAttribute.endsWith( ":*"))
        {
          String prefix = staticAttribute.substring( 0, staticAttribute.length()-2);
          if ( attribute.startsWith( prefix)) return true;
        }
        else if ( staticAttribute.equals( "*") || attribute.equals( staticAttribute))
        {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * This method conditions the reference before performing the sync. 
   * The dirty flag is cleared and the reference is added to the cache.
   * @param reference The reference to be synced.
   */
  protected void internal_sync( IExternalReference reference) throws CachingException
  {
    // check sync lock before proceeding
    IModel model = reference.getModel();
    if ( model.getSyncLock()) return;
    
    // clear dirty flag
    reference.setDirty( false);
    
    // unlock reference so that the reference can be updated in the stack frame of the
    // notification for its being inserted into the model dirty
    boolean wasLocked = (model.isLocked( reference) != null);
    model.unlock( reference);
    
    try
    {
      // sync
      sync( reference);
    }
    finally
    {
      // reference enters cache when it is first synced
      if ( cache != null) cache.add( reference);
      
      // relock reference if it was previously locked
      if ( wasLocked) model.lock( reference);
    }
  }

  private ICache cache;
  private IXmlIO xmlIO;
  private String[] staticAttributes;
}
