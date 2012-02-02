/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * FileSystemCachingPolicy.java
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.ICachingPolicy;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.UnboundedCache;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;

/**
 * A ConfiguredCachingPolicy which creates a datamodel for a file or folder of the file system.
 * The datamodel recursively creates external references for each nested xml file or folder. An element
 * representing an xml file or folder is named with the file name. The root external reference must
 * define the <i>path</i> attribute which specifies the base path in the file system.  The name of 
 * the root element is appended to the base path to find the absolute path of the root.
 * <p>
 * Files which end with .xip are associated with the TabularCompressor compression format.
 * <p>
 * NOTE: This caching policy must be unique for each root external reference.
 */
public class FileSystemCachingPolicy extends ConfiguredCachingPolicy
{
  public FileSystemCachingPolicy()
  {
    this( new UnboundedCache());
  }
  
  public FileSystemCachingPolicy( ICache cache)
  {
    super( cache);
    
    setStaticAttributes( new String[] { "*"});
    defineNextStage( XPath.createExpression( "*"), this, true);
    
    associations = new HashMap<String, IFileAssociation>();
    addAssociation( csvAssociation);
    addAssociation( txtAssociation);
    addAssociation( xipAssociation);
    addAssociation( xmlAssociation);
    addAssociation( zipAssociation);
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
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    // save root
    if ( fileSystemRoot == null) 
    {
      fileSystemRoot = reference;
      replaceTilde( fileSystemRoot);
    }
   
    // just in case
    reference.removeChildren();

    // sync
    File path = new File( Xlate.get( reference, "path", ""));

    if ( path.isDirectory())
    {
      for( String member: path.list())
      {
        int index = member.lastIndexOf( '.');
        String extension = (index >= 0)? member.substring( index): (String)null;
        IFileAssociation association = associations.get( extension);
        ICachingPolicy cachingPolicy = (association != null)? association.getCachingPolicy( this, member): null;
        if ( cachingPolicy == null) cachingPolicy = this;
        IExternalReference child = (IExternalReference)reference.createObject( member);
        child.setCachingPolicy( cachingPolicy);
        child.setDirty( true);
        reference.addChild( child);
        child.setAttribute( "path", buildChildPath( child));
      }
    }
    else if ( path.exists() && path.canRead())
    {
      String name = path.getName();
      int index = name.lastIndexOf( '.');
      String extension = (index >= 0)? name.substring( index): (String)null;
      IFileAssociation association = associations.get( extension);
      if ( association != null) 
      {
        try
        {
          FileInputStream stream = new FileInputStream( path);
          association.apply( reference, path.getPath(), stream);
          stream.close();
        }
        catch( IOException e)
        {
          throw new CachingException( "Unable to read file: "+path, e);
        }
      }
    }
  }
  
  /**
   * Replace the tilde at the beginning of the path of the specified element.
   * @param element The element.
   */
  private static void replaceTilde( IModelObject element)
  {
    String path = Xlate.get( element, "path", "");
    if ( path.length() > 0 && path.charAt( 0) == '~')
    {
      String userDir = System.getProperty( "user.dir");
      Xlate.set( element, "path", userDir + path.substring( 1));
    }
  }
  
  /**
   * Build the path for the specified file system element.
   * @param element The file system element.
   * @return Returns the absolute path.
   */
  private File buildChildPath( IModelObject element) throws CachingException
  {
    String basePath = Xlate.get( element.getParent(), "path", "");
    return new File( basePath, element.getType());
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#flushImpl(org.xmodel.external.IExternalReference)
   */
  public void flushImpl( IExternalReference reference) throws CachingException
  {
    File path = new File( Xlate.get( reference, "path", ""));
    if ( path.isDirectory())
      throw new CachingException( 
        "Directory cannot be flushed: "+reference);
    
    try
    {
      (new XmlIO()).write( reference.getChild( 0), path);
    }
    catch( XmlException e)
    {
      throw new CachingException( "Unable to flush reference: "+reference, e);
    }
  }
  
  private final static IFileAssociation csvAssociation = new CsvAssociation();
  private final static IFileAssociation txtAssociation = new TxtAssociation();
  private final static IFileAssociation xipAssociation = new XipAssociation();
  private final static IFileAssociation xmlAssociation = new XmlAssociation();
  private final static IFileAssociation zipAssociation = new ZipAssociation();

  private IExternalReference fileSystemRoot;
  private Map<String, IFileAssociation> associations;
}
