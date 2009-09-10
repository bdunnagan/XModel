/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.external.caching;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.UnboundedCache;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


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
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#configure(org.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    // Currently, associations are not configurable because there is no ClassLoader available here.
    // Create a sub-class which extends the associations instead.
    parentContext = context;
    pathExpr = Xlate.get( annotation, "path", defaultPathExpr);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    // save root
    if ( fileSystemRoot == null) fileSystemRoot = reference;
   
    // just in case
    reference.removeChildren();
    
    // sync
    File path = buildPath( reference);
    if ( path.isDirectory())
    {
      for( String member: path.list())
      {
        IExternalReference child = (IExternalReference)reference.createObject( member);
        child.setCachingPolicy( this);
        child.setDirty( true);
        reference.addChild( child);
        child.setAttribute( "path", buildPath( child));
      }
    }
    else if ( path.exists() && path.canRead())
    {
      // populate path on leaf
      //try { reference.setAttribute( "path", path.getCanonicalPath());} catch( IOException e) {}
      
      // sync
      String name = path.getName();
      int index = name.lastIndexOf( '.');
      if ( index >= 0)
      {
        String extension = name.substring( index);
        IFileAssociation association = associations.get( extension);
        if ( association != null) association.apply( reference, path);
      }
    }
  }
  
  /**
   * Build the path for the specified file system element.
   * @param element The file system element.
   * @return Returns the absolute path.
   */
  public File buildPath( IModelObject element) throws CachingException
  {
    // get levels
    List<String> levels = new ArrayList<String>();
    while( element != fileSystemRoot)
    {
      levels.add( element.getType());
      element = element.getParent();
    }
    levels.add( fileSystemRoot.getType());
      
    // get base path
    String userDir = System.getProperty( "user.dir");
    String basePath = pathExpr.evaluateString( new Context( parentContext, fileSystemRoot));
    basePath = basePath.replaceFirst( "\\~", userDir.replaceAll( "\\\\", "\\\\\\\\"));
    
    // build path
    StringBuilder path = new StringBuilder();
    path.append( basePath);
    
    for( int i=levels.size()-1; i>=0; i--)
    {
      path.append( File.separatorChar);
      path.append( levels.get( i));
    }
    
    return new File( path.toString());
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#flush(org.xmodel.external.IExternalReference)
   */
  public void flush( IExternalReference reference) throws CachingException
  {
    File path = buildPath( reference);
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
  
  /* (non-Javadoc)
   * @see org.xmodel.external.AbstractCachingPolicy#getURI(org.xmodel.external.IExternalReference)
   */
  @Override
  public URI getURI( IExternalReference reference) throws CachingException
  {
    return buildPath( reference).toURI();
  }

  private final IExpression defaultPathExpr = XPath.createExpression( "@path");
  
  private final static IFileAssociation txtAssociation = new TxtAssociation();
  private final static IFileAssociation xipAssociation = new XipAssociation();
  private final static IFileAssociation xmlAssociation = new XmlAssociation();
  
  private IContext parentContext;
  private IExpression pathExpr;
  private IExternalReference fileSystemRoot;
  private Map<String, IFileAssociation> associations;
}
