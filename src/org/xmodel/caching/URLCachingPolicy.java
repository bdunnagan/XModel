/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * URLCachingPolicy.java
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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.xmodel.INode;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.UnboundedCache;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;


public class URLCachingPolicy extends ConfiguredCachingPolicy
{
  public URLCachingPolicy()
  {
    this( new UnboundedCache());
  }
  
  /**
   * Create a URLCachingPolicy which uses the specified cache.
   * @param cache The cache.
   */
  public URLCachingPolicy( ICache cache)
  {
    super( cache);
   
    setStaticAttributes( new String[] { "id", "url"});

    associations = new HashMap<String, IFileAssociation>();
    addAssociation( csvAssociation);
    addAssociation( txtAssociation);
    addAssociation( xipAssociation);
    addAssociation( xmlAssociation);
    
    // default behavior when not configured
    urlExpr = defaultUrlExpr;
  }

  /**
   * Add the specified file association.
   * @param association The association.
   */
  public void addAssociation( IFileAssociation association)
  {
    for( String extension: association.getExtensions())
      associations.put( extension, association);
  }
  
  /**
   * Remove the specified file extension association.
   * @param extension The file extension (including the dot).
   */
  public void removeAssociation( String extension)
  {
    associations.remove( extension);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#configure(org.xmodel.xpath.expression.IContext, org.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, INode annotation) throws CachingException
  {
    super.configure( context, annotation);
    
    urlExpr = Xlate.get( annotation, "url", (IExpression)null);
    if ( urlExpr == null) urlExpr = defaultUrlExpr;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    String string = urlExpr.evaluateString( new StatefulContext( reference));
    if ( string == null) return;

    URL url = null;
    try
    {
      int index = string.lastIndexOf( '.');
      if ( index >= 0)
      {
        String extension = string.substring( index);
        IFileAssociation association = associations.get( extension);
        if ( association != null) 
        {
          url = new URL( string);
          association.apply( reference, reference.getType(), url.openStream());
        }
      }
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to sync url: "+url, e);
    }
  }

  private final static IFileAssociation csvAssociation = new CsvAssociation();
  private final static IFileAssociation txtAssociation = new TxtAssociation();
  private final static IFileAssociation xipAssociation = new XipAssociation();
  private final static IFileAssociation xmlAssociation = new XmlAssociation();

  private final static IExpression defaultUrlExpr = XPath.createExpression( "@url");
  
  private IExpression urlExpr;
  private Map<String, IFileAssociation> associations;
}
