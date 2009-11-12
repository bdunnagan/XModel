/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * DefaultSpace.java
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
package org.xmodel.external;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xml.XmlIO;

/**
 * The default external space which uses URL.openStream() to parse the XML.
 */
public class DefaultSpace implements IExternalSpace
{
  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalSpace#contains(java.net.URI)
   */
  public boolean contains( URI uri)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.IExternalSpace#query(java.net.URI)
   */
  public List<IModelObject> query( URI uri) throws CachingException
  {
    try
    {
      XmlIO xmlIO = new XmlIO();
      IModelObject element = xmlIO.read( uri.toURL().openStream());
      return Collections.singletonList( element);
    }
    catch( MalformedURLException e)
    {
      throw new CachingException( "Unable to perform URI query:", e);
    }
    catch( Exception e)
    {
      return null;
    }
  }
}
