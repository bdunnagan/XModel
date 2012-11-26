/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ZipCachingPolicy.java
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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.xmodel.INode;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ExternalReference;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.UnboundedCache;
import org.xmodel.log.SLog;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * A caching policy for zip files. This caching policy loads the contents of the zip file in stages.
 */
public class ZipCachingPolicy extends ConfiguredCachingPolicy
{
  public ZipCachingPolicy()
  {
    this( new UnboundedCache());
  }
  
  public ZipCachingPolicy( ICache cache)
  {
    super( cache);
    
    setStaticAttributes( new String[] { "path", "zipFile"});
    defineNextStage( XPath.createExpression( ".//*[ @entry]"), new ZipEntryCachingPolicy(), true);
    
    associations = new HashMap<String, IFileAssociation>();
    addAssociation( txtAssociation);
    addAssociation( xipAssociation);
    addAssociation( xmlAssociation);
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
  
  /**
   * Returns the IFileAssociation for the specified extension.
   * @param extension The extension.
   * @return Returns null or the IFileAssociation for the specified extension.
   */
  public IFileAssociation getAssociation( String extension)
  {
    return associations.get( extension);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    File file = new File( Xlate.get( reference, "path", "."));
    SLog.infof( this, "LOADING %s", file.getAbsoluteFile());
    if ( file.canRead())
    {
      try
      {
        ZipFile zipFile = new ZipFile( file);
        reference.setAttribute( "zipFile", zipFile);
        
        INode clone = reference.cloneObject();
                
        // create elements for root entries
        String separator = null;
        Enumeration<? extends ZipEntry> iter = zipFile.entries();
        while( iter.hasMoreElements())
        {
          ZipEntry entry = iter.nextElement();
          if ( entry.isDirectory()) continue;
          
          String path = entry.getName();
          
          if ( separator == null) 
          {
            separator = determinePathSeparator( path);
          }
          
          // populate entire directory tree
          StatefulContext context = new StatefulContext( reference);
          context.set( "path", path);

          // only include files that have an extension with an association
          boolean include = false;
          int index = path.lastIndexOf( '.');
          if ( index >= 0)
          {
            String extension = path.substring( index);
            include = associations.get( extension) != null;
          }
          
          if ( include)
          {
            if ( separator != null)
            {
              INode element = clone;
              StringTokenizer tokenizer = new StringTokenizer( path, separator);
              while( tokenizer.hasMoreTokens())
              {
                String token = tokenizer.nextToken();
                element = element.getCreateChild( token);
              }
  
              if ( !entry.isDirectory())
                element.setAttribute( "entry", entry);
            }
            else
            {
              INode element = new ModelObject( entry.getName());
              element.setAttribute( "entry", entry);
              clone.addChild( element);
            }
          }
        }
        
        clone.setAttribute( "separator", separator);
        update( reference, clone);
      }
      catch( IOException e)
      {
        throw new CachingException( "Unable to load zip file: "+file, e);
      }
    }
  }
  
  
  /**
   * Determines the path separator character by scanning the path.
   * @param path The path.
   * @return Returns null or the path separator.
   */
  private static String determinePathSeparator( String path)
  {
    int index = path.indexOf( "\\ ");
    if ( index >= 0) return "/";
    
    index = path.indexOf( "/");
    if ( index >= 0) return "/";
    
    return null;
  }
  
  private final static IFileAssociation txtAssociation = new TxtAssociation();
  private final static IFileAssociation xipAssociation = new XipAssociation();
  private final static IFileAssociation xmlAssociation = new XmlAssociation();

  private Map<String, IFileAssociation> associations;
  
  public static void main( String[] args) throws Exception
  {
    StringTokenizer tokener = new StringTokenizer( "main.xml", "/");
    if ( tokener.hasMoreTokens()) System.out.println( tokener.nextToken());
    System.exit( 1);
    
    IExternalReference reference = new ExternalReference( "zip");
    reference.setCachingPolicy( new ZipCachingPolicy());
    reference.setAttribute( "path", "/Users/bdunnagan/xmodel.jar");
    reference.setDirty( true);
    
    System.out.println( XmlIO.toString( reference));
  }
}
