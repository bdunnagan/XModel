/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * TxtAssociation.java
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.xmodel.IModelObject;
import org.xmodel.external.CachingException;

/**
 * An IFileAssociation for comma-separator value text files with the .csv extension. These
 * files are parsed into "entry" elements with one attribute for each field. The attributes
 * are enumerated "f1" through "fN", where N is the number of fields.
 */
public class CsvAssociation implements IFileAssociation
{
  /* (non-Javadoc)
   * @see org.xmodel.external.caching.IFileAssociation#getAssociations()
   */
  public String[] getExtensions()
  {
    return extensions;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.caching.IFileAssociation#apply(org.xmodel.IModelObject, java.lang.String, java.io.InputStream)
   */
  public void apply( IModelObject parent, String name, InputStream stream) throws CachingException
  {
    try
    {
      char[] buffer = new char[ 1 << 16];
      StringBuilder content = new StringBuilder();
      BufferedReader reader = new BufferedReader( new InputStreamReader( stream));
      while( reader.ready())
      {
        int count = reader.read( buffer, 0, buffer.length);
        if ( count > 0) content.append( buffer, 0, count);
      }
      parent.setValue( content.toString());
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable read text file: "+name, e);
    }
  }
  
  private final static String[] extensions = { ".csv"};
}
