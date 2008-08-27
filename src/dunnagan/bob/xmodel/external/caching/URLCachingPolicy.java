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
import dunnagan.bob.xmodel.xpath.expression.IContext;

public class URLCachingPolicy extends ConfiguredCachingPolicy
{
  /**
   * Create a URLCachingPolicy which uses the specified cache.
   * @param cache The cache.
   */
  public URLCachingPolicy( ICache cache)
  {
    super( cache);
    setStaticAttributes( new String[] { "id", "url"});
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ConfiguredCachingPolicy#configure(dunnagan.bob.xmodel.xpath.expression.IContext, dunnagan.bob.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    super.configure( context, annotation);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ConfiguredCachingPolicy#syncImpl(dunnagan.bob.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    String string = Xlate.get( reference, "url", (String)null);
    if ( string == null) return;

    XmlIO xmlIO = new XmlIO();
    xmlIO.setFactory( getFactory());
    
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
}
