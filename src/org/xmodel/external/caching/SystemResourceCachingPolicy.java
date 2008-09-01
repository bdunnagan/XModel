/*
 * Stonewall Networks, Inc.
 *
 *   Project: XModel
 *   Author:  bdunnagan
 *   Date:    Apr 22, 2008
 *
 * Copyright 2008.  Stonewall Networks, Inc.
 */
package org.xmodel.external.caching;

import java.net.URL;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;


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
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
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
