/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * XmlAssociation.java
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
package org.xmodel.caching;

import java.io.InputStream;
import org.xmodel.IModelObject;
import org.xmodel.external.CachingException;
import org.xmodel.xml.XmlIO;

/**
 * An IFileAssociation for xml, xsd, dtd and other well-formed xml extensions.
 */
public class XmlAssociation extends AbstractFileAssociation
{
  /* (non-Javadoc)
   * @see org.xmodel.external.caching.IFileAssociation#getAssociations()
   */
  public String[] getExtensions()
  {
    return extensions;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.caching.IFileAssociation#apply(org.xmodel.IModelObject, java.io.File)
   */
  public void apply( IModelObject parent, String name, InputStream stream) throws CachingException
  {
    int count = 0;
    try
    {
      count = stream.available();
      XmlIO xmlIO = new XmlIO();
      IModelObject content = xmlIO.read( stream);
      parent.addChild( content);
    }
    catch( Exception e)
    {
      // see if file is empty
      if ( count == 0) return;
      
      // this is an xml parsing error
      throw new CachingException( "Unable to parse xml in file: "+name, e);
    }
  }

  private final static String[] extensions = { ".xml", ".xsd", ".dtd"};
}
