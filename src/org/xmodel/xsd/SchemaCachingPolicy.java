/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SchemaCachingPolicy.java
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
package org.xmodel.xsd;

import java.net.URL;
import org.xmodel.INode;
import org.xmodel.ModelAlgorithms;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;


/**
 * A caching policy for loading schemas.  The caching policy loads all imports and includes when
 * the reference is synchronized to form a complete schema with one root.  The target of the 
 * reference is defined in the <i>url</i> attribute.
 */
public class SchemaCachingPolicy extends ConfiguredCachingPolicy
{
  /**
   * Create caching policy with the specified cache.
   * @param cache The cache.
   */
  public SchemaCachingPolicy( ICache cache)
  {
    super( cache);
    setStaticAttributes( new String[] { "id", "url", "unordered", "cachingPolicy"});
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    String string = Xlate.get( reference, "url", (String)null);
    if ( string == null) return;

    URL url = null;
    try
    {
      url = new URL( string);
      INode urlObject = reference.cloneObject();
      Xsd xsd = new Xsd( url);
      unordered = Xlate.get( reference, "unordered", false);
      SchemaTransform transform = new SchemaTransform( unordered);
      urlObject.addChild( transform.transform( xsd.getRoot()));
      update( reference, urlObject);
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to sync url: "+url, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#insert(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject, boolean)
   */
  public void insert( IExternalReference parent, INode object, int index, boolean dirty) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#remove(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, INode object) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#update(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject)
   */
  public void update( IExternalReference reference, INode object) throws CachingException
  {
    // cannot use differ here because object contains reference cycles
    reference.removeChildren();
    
    // copy attributes and children
    ModelAlgorithms.copyAttributes( object, reference);
    ModelAlgorithms.moveChildren( object, reference);
  }

  private boolean unordered;
}
