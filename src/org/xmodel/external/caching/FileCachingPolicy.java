/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.external.caching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URI;
import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * An implementation of ICachingPolicy for files containing XML documents.
 */
public class FileCachingPolicy extends ConfiguredCachingPolicy
{
  /**
   * Create a URLCachingPolicy which uses the specified cache.
   * @param cache The cache.
   */
  public FileCachingPolicy( ICache cache)
  {
    super( cache);
    setStaticAttributes( new String[] { "*"});
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#configure(org.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    parentContext = context;
    pathExpr = Xlate.get( annotation, "path", defaultPathExpr);
    create = Xlate.get( annotation, "create", false);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#checkout(
   * org.xmodel.external.IExternalReference)
   */
  public void checkout( IExternalReference reference)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#checkin(
   * org.xmodel.external.IExternalReference)
   */
  public void checkin( IExternalReference reference)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  public void syncImpl( IExternalReference reference) throws CachingException
  {
    String path = pathExpr.evaluateString( new Context( parentContext, reference));
    if ( path == null) throw new CachingException( "File reference path not defined.");
    
    File file = new File( path);
    try
    {
      FileInputStream stream = new FileInputStream( file);
      IModelObject fileObject = reference.cloneObject();
      IModelObject rootTag = (new XmlIO()).read( stream);
      ModelAlgorithms.moveChildren( rootTag, fileObject);
      update( reference, fileObject);
    }
    catch( FileNotFoundException e)
    {
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to sync file: "+file, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#flush(
   * org.xmodel.external.IExternalReference)
   */
  public void flush( IExternalReference reference) throws CachingException
  {
    if ( reference.isDirty()) return;
    
    String path = pathExpr.evaluateString( new Context( parentContext, reference));
    if ( path == null) throw new CachingException( "File reference path not defined.");
    
    File file = new File( path);
    try
    {
      if ( create) file.createNewFile();
      FileOutputStream stream = new FileOutputStream( file);
      (new XmlIO()).write( reference, stream);
      stream.close();
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to flush changes to file: "+path, e);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#insert(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject, boolean)
   */
  public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#remove(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.AbstractCachingPolicy#getURI(org.xmodel.external.IExternalReference)
   */
  @Override
  public URI getURI( IExternalReference reference) throws CachingException
  {
    String path = pathExpr.evaluateString( new Context( parentContext, reference));
    if ( path == null) throw new CachingException( "File reference path not defined.");
    
    File file = new File( path);
    return file.toURI();
  }

  private final IExpression defaultPathExpr = XPath.createExpression( "@path");
  
  private IContext parentContext;
  private IExpression pathExpr = defaultPathExpr;
  private boolean create;
}
