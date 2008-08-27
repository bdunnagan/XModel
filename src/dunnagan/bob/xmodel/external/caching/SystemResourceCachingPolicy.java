/*
 * Stonewall Networks, Inc.
 *
 *   Project: XModel
 *   Author:  bdunnagan
 *   Date:    Apr 22, 2008
 *
 * Copyright 2008.  Stonewall Networks, Inc.
 */
package dunnagan.bob.xmodel.external.caching;

import java.net.URL;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.external.CachingException;
import dunnagan.bob.xmodel.external.ConfiguredCachingPolicy;
import dunnagan.bob.xmodel.external.ICache;
import dunnagan.bob.xmodel.external.IExternalReference;
import dunnagan.bob.xmodel.xml.XmlException;
import dunnagan.bob.xmodel.xml.XmlIO;

/**
 * An ICachingPolicy which will load an XML file which is accessible via ClassLoader.getSystemResource which 
 * means that the XML file is on the system classpath. There is no configuration for this policy, but the
 * reference should defined the <i>path</i> attribute.
 */
public class SystemResourceCachingPolicy extends ConfiguredCachingPolicy
{
  public SystemResourceCachingPolicy( ICache cache)
  {
    super( cache);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ConfiguredCachingPolicy#syncImpl(dunnagan.bob.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    String path = Xlate.get( reference, "path", "undefined");
    URL url = ClassLoader.getSystemResource( path);
    try
    {
      XmlIO xmlIO = new XmlIO();
      xmlIO.setFactory( getFactory());
      IModelObject object = xmlIO.read( url);
      if ( object != null) update( reference, object);
    }
    catch( XmlException e)
    {
      throw new CachingException( "Unable to sync reference: "+reference, e);
    }
  }
}
