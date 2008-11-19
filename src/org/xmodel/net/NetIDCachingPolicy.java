/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.net;

import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;

/**
 * An ICachingPolicy which resolves itself using a ModelClient instance.
 */
public class NetIDCachingPolicy extends ConfiguredCachingPolicy
{
  public NetIDCachingPolicy( ICache cache, ModelClient client)
  {
    super( cache);
    this.client = client;
    setStaticAttributes( new String[] { "id", "xm:*", "net:*"});
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    reference.removeChildren();
    
    try
    {
      // bind
      IModelObject result = client.sync( reference);
      if ( result == null) throw new CachingException( "Network operation timed out.");

      // update reference
      ModelAlgorithms.copyAttributes( result, reference);
      IModelObject[] array = result.getChildren().toArray( new IModelObject[ 0]);
      for( IModelObject child: array) reference.addChild( child);
    }
    catch( TimeoutException e)
    {
      throw new CachingException( "Unable to bind reference: "+reference, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.AbstractCachingPolicy#clear(org.xmodel.external.IExternalReference)
   */
  @Override
  public void clear( IExternalReference reference) throws CachingException
  {
    // send clear message
    try
    {
      String netID = Xlate.get( reference, "net:id", "");
      assert( netID.length() > 0);
      client.unbind( netID);
    }
    catch( TimeoutException e)
    {
    }
    
    // clear
    super.clear( reference);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#flush(org.xmodel.external.IExternalReference)
   */
  public void flush( IExternalReference reference) throws CachingException
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#insert(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject, int, boolean)
   */
  public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#remove(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
  }
  
  private ModelClient client;
}
