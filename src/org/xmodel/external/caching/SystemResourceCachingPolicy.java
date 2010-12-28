/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SystemResourceCachingPolicy.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
