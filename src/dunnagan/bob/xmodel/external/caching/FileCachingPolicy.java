/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external.caching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelAlgorithms;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.external.CachingException;
import dunnagan.bob.xmodel.external.ConfiguredCachingPolicy;
import dunnagan.bob.xmodel.external.ICache;
import dunnagan.bob.xmodel.external.IExternalReference;
import dunnagan.bob.xmodel.xml.XmlIO;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.Context;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

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
   * @see dunnagan.bob.xmodel.external.ConfiguredCachingPolicy#configure(dunnagan.bob.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    parentContext = context;
    pathExpr = Xlate.get( annotation, "path", defaultPathExpr);
    create = Xlate.get( annotation, "create", false);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#checkout(
   * dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void checkout( IExternalReference reference)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#checkin(
   * dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void checkin( IExternalReference reference)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ConfiguredCachingPolicy#syncImpl(dunnagan.bob.xmodel.external.IExternalReference)
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
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#flush(
   * dunnagan.bob.xmodel.external.IExternalReference)
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
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#insert(dunnagan.bob.xmodel.external.IExternalReference, 
   * dunnagan.bob.xmodel.IModelObject, boolean)
   */
  public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#remove(dunnagan.bob.xmodel.external.IExternalReference, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  private final static IExpression defaultPathExpr = XPath.createExpression( 
    "@path");
  
  private IContext parentContext;
  private IExpression pathExpr = defaultPathExpr;
  private boolean create;
}
