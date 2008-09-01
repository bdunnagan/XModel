/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xsd;

import java.net.URL;
import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;


/**
 * A caching policy for loading schemas.  The caching policy loads all imports and includes when
 * the reference is synchronized to form a complete schema with one root.  The target of the 
 * reference is defined in the <i>url</i> attribute.
 */
public class SchemaCachingPolicy extends ConfiguredCachingPolicy
{
  /**
   * Create caching policy with the specified cache.
   * @param cache The cache.
   */
  public SchemaCachingPolicy( ICache cache)
  {
    super( cache);
    setStaticAttributes( new String[] { "id", "url", "unordered", "cachingPolicy"});
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    String string = Xlate.get( reference, "url", (String)null);
    if ( string == null) return;

    URL url = null;
    try
    {
      url = new URL( string);
      IModelObject urlObject = reference.cloneObject();
      Xsd xsd = new Xsd( url);
      unordered = Xlate.get( reference, "unordered", false);
      SchemaTransform transform = new SchemaTransform( unordered);
      urlObject.addChild( transform.transform( xsd.getRoot()));
      update( reference, urlObject);
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to sync url: "+url, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#flush(org.xmodel.external.IExternalReference)
   */
  public void flush( IExternalReference reference) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#insert(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject, boolean)
   */
  public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#remove(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#update(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject)
   */
  public void update( IExternalReference reference, IModelObject object) throws CachingException
  {
    // cannot use differ here because object contains reference cycles
    reference.removeChildren();
    
    // copy attributes and children
    ModelAlgorithms.copyAttributes( object, reference);
    ModelAlgorithms.moveChildren( object, reference);
  }

  private boolean unordered;
}
