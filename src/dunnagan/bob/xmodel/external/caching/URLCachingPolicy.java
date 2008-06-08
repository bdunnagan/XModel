/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external.caching;

import java.net.URL;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.external.*;
import dunnagan.bob.xmodel.xml.XmlIO;

public class URLCachingPolicy extends AbstractCachingPolicy
{
  /**
   * Create a URLCachingPolicy which uses the specified cache.
   * @param cache The cache.
   */
  public URLCachingPolicy( ICache cache)
  {
    super( cache);
    setStaticAttributes( new String[] { "id", "url"});
    xmlIO = new XmlIO();
    differ = new EntityDiffer();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#sync(dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void sync( IExternalReference reference) throws CachingException
  {
    String string = Xlate.get( reference, "url", (String)null);
    if ( string == null) return;

    URL url = null;
    try
    {
      url = new URL( string);
      IModelObject urlObject = reference.cloneObject();
      IModelObject rootTag = xmlIO.read( url.openStream());
      urlObject.addChild( rootTag);
      update( reference, urlObject);
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to sync url: "+url, e);
    }
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
   * dunnagan.bob.xmodel.IModelObject, boolean)
   */
  public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#remove(dunnagan.bob.xmodel.external.IExternalReference, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#update(dunnagan.bob.xmodel.external.IExternalReference, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  public void update( IExternalReference reference, IModelObject object) throws CachingException
  {
    differ.diffAndApply( reference, object);
  }

  XmlIO xmlIO;
  EntityDiffer differ;
}
