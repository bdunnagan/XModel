/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external.caching;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import dunnagan.bob.xmodel.*;
import dunnagan.bob.xmodel.diff.XmlDiffer;
import dunnagan.bob.xmodel.external.*;
import dunnagan.bob.xmodel.xml.XmlIO;
import dunnagan.bob.xmodel.xpath.XPath;

/**
 * A ConfiguredCachingPolicy which creates a datamodel for a file or folder of the file system.
 * The datamodel recursively creates external references for each nested file or folder. An element
 * representing a file or folder is named with the file name. The root of the file-system data-model
 * must have an attribute <i>path</i> which is the absolute file system path of the root element.
 * <p>
 * The user.dir and user.home system properties are defined as variables before evaluating the path.
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
    init();
  }

  private void init()
  {
    xmlIO = new XmlIO();
    differ = new XmlDiffer();
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ConfiguredCachingPolicy#syncImpl(dunnagan.bob.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    File path = buildPath( reference);
    if ( path.isDirectory())
    {
      IModelObject element = new ModelObject( reference.getType());
      ModelAlgorithms.copyAttributes( reference, element);
      
      List<ExternalReference> children = new ArrayList<ExternalReference>();
      for( String member: path.list())
      {
        ExternalReference child = new ExternalReference( member);
        element.addChild( child);
        children.add( child);
      }

      update( reference, element);

      for( ExternalReference child: children)
        child.setCachingPolicy( this, true);
    }
    else
    {
      IModelObject element = new ModelObject( path.getName());
      ModelAlgorithms.copyAttributes( reference, element);
      
      try
      {
        IModelObject content = xmlIO.read( new FileInputStream( path));
        element.addChild( content);
        update( reference, element);
      }
      catch( Exception e)
      {
        throw new CachingException( "Unable to sync reference: "+reference, e);
      }
    }
  }
  
  /**
   * Build the path for the specified file system element.
   * @param element The file system element.
   * @return Returns the absolute path.
   */
  private File buildPath( IModelObject element) throws CachingException
  {
    IModelObject fileSystemRoot = fileSystemRootPath.queryFirst( element);
    if ( fileSystemRoot == null)
      throw new CachingException(
        "Unable to find root of file system data-model denoted by path attribute.");

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
    String basePath = Xlate.get( fileSystemRoot, "path", "");
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
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#flush(dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void flush( IExternalReference reference) throws CachingException
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#insert(dunnagan.bob.xmodel.external.IExternalReference, 
   * dunnagan.bob.xmodel.IModelObject, boolean)
   */
  public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ConfiguredCachingPolicy#update(dunnagan.bob.xmodel.external.IExternalReference, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  @Override
  public void update( IExternalReference reference, IModelObject object) throws CachingException
  {
    differ.diffAndApply( reference, object);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#remove(dunnagan.bob.xmodel.external.IExternalReference, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
  }

  private final static IPath fileSystemRootPath = XPath.createPath( 
    "ancestor-or-self::*[ boolean( @path)]");
  
  private XmlIO xmlIO;
  private XmlDiffer differ;
}
