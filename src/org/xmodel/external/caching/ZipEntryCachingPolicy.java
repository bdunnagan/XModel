/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ZipEntryCachingPolicy.java
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

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.xmodel.IModelObject;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.UnboundedCache;

/**
 * A caching policy for loading a zip file entry.
 */
public class ZipEntryCachingPolicy extends ConfiguredCachingPolicy
{
  public ZipEntryCachingPolicy()
  {
    this( new UnboundedCache());
  }
  
  public ZipEntryCachingPolicy( ICache cache)
  {
    super( cache);
    setStaticAttributes( new String[] { "entry"});
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    ZipEntry thisEntry = (ZipEntry)reference.getAttribute( "entry");
    if ( thisEntry == null) return;
    
    IExternalReference zipReference = getZipReference( reference);
    ZipFile zipFile = (ZipFile)zipReference.getAttribute( "zipFile");
    
    try
    {
      reference.removeChildren();
      InputStream stream = zipFile.getInputStream( thisEntry);
      
      int index = thisEntry.getName().lastIndexOf( ".");
      String extension = thisEntry.getName().substring( index);
      System.out.println( "ext="+extension);
      
      ZipCachingPolicy cachingPolicy = (ZipCachingPolicy)zipReference.getCachingPolicy();
      IFileAssociation association = cachingPolicy.getAssociation( extension);
      association.apply( reference, thisEntry.getName(), stream);
      
      stream.close();
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to load zip entry: "+thisEntry, e);
    }
  }
  
  /**
   * Returns the ancestor of the specified reference that has the ZipCachingPolicy.
   * @param reference The starting point of the search.
   * @return Returns the ancestor of the specified reference that has the ZipCachingPolicy.
   */
  private IExternalReference getZipReference( IExternalReference reference)
  {
    IModelObject ancestor = reference;
    while( ancestor != null)
    {
      if ( ancestor instanceof IExternalReference)
      {
        reference = (IExternalReference)ancestor;
        if ( reference.getCachingPolicy() instanceof ZipCachingPolicy)
          return reference;
      }
      ancestor = ancestor.getParent();
    }
    return null;
  }
}
