/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * XipAssociation.java
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
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.xmodel.IModelObject;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.external.CachingException;

/**
 * An IFileAssociation for the XModel <i>.xip</i> extension associated with the TabularCompressor.
 */
public class XipAssociation extends AbstractFileAssociation
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
      ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
      while( buffer.writeBytes( stream, 1024) == 1024);
      
      TabularCompressor compressor = new TabularCompressor();
      IModelObject content = compressor.decompress( buffer);
      parent.addChild( content);
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to parse xml in compressed file: "+name, e);
    }
  }
  
  private final static String[] extensions = { ".xip"};
}
