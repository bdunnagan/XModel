/*
 * Stonewall Networks, Inc.
 *
 *   Project: XModel
 *   Author:  bdunnagan
 *   Date:    Apr 4, 2008
 *
 * Copyright 2008.  Stonewall Networks, Inc.
 */
package dunnagan.bob.xmodel.external.caching;

import java.lang.reflect.Constructor;
import java.util.List;

import dunnagan.bob.xmodel.IBoundChangeRecord;
import dunnagan.bob.xmodel.IChangeRecord;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IModelObjectFactory;
import dunnagan.bob.xmodel.ModelAlgorithms;
import dunnagan.bob.xmodel.ModelObjectFactory;
import dunnagan.bob.xmodel.UnionChangeSet;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.diff.XmlDiffer;
import dunnagan.bob.xmodel.external.ConfiguredCachingPolicy;
import dunnagan.bob.xmodel.external.ICache;
import dunnagan.bob.xmodel.external.IExternalReference;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.Context;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * A class which performs the destructive caching policy annotation transform. This transform
 * consists of replacing elements in the input tree which have a child <i>extern:cache</i> with an
 * instance of IExternalReference which is given a ConfiguredCachingPolicy created and configured
 * from the <i>extern:cache</i> annotation.
 */
public class AnnotationTransform
{
  public AnnotationTransform()
  {
    loader = getClass().getClassLoader();
    factory = new ModelObjectFactory();
  }
  
  /**
   * Set the ClassLoader used to load ConfiguredCachingPolicy classes.
   * @param loader The loader.
   */
  public void setClassLoader( ClassLoader loader)
  {
    this.loader = loader;
  }
  
  /**
   * Set the factory.
   * @param factory The factory.
   */
  public void setFactory( IModelObjectFactory factory)
  {
    this.factory = factory;
  }
  
  /**
   * Set the parent context used to resolve variables in <i>extends</i> expressions.
   * @param context The parent context.
   */
  public void setParentContext( IContext context)
  {
    this.parent = context;
  }
  
  /**
   * Return the transform of the specified element.  
   * @param root The element to be transformed.
   * @return Returns the transform of the specified element.
   */
  public IModelObject transform( IModelObject root)
  {
    IModelObject result = root;
    
    expandAllAnnotations( root);
    
    for( IModelObject annotated: annotatedExpr.query( root, null))
    {
      IModelObject annotation = annotated.getFirstChild( "extern:cache");
      //annotation.removeFromParent();

      if ( annotated.isType( "extern:match"))
      {
        ConfiguredCachingPolicy cachingPolicy = createCachingPolicy( annotation, annotated.getChildren());
        annotated.setAttribute( "extern:cp", cachingPolicy);
      }
      else
      {
        IModelObject transformed = transform( annotated, annotation);
  
        IModelObject parent = annotated.getParent();
        if ( parent != null)
        {
          int index = parent.getChildren().indexOf( annotated);
          annotated.removeFromParent();
          parent.addChild( transformed, index);
        }
        
        if ( annotated == root) result = transformed; 
      }
      
      // some caching policies rely on their the configuration of their ancestors
      annotation.removeFromParent();
    }
    
    return result;
  }
  
  /**
   * Transform the specified annotated element.
   * @param element The element.
   * @param annotation The annotation.
   * @return Returns the transformed element.
   */
  public IModelObject transform( IModelObject element, IModelObject annotation)
  {
    ConfiguredCachingPolicy cachingPolicy = createCachingPolicy( annotation, element.getChildren());
    
    IExternalReference reference = factory.createExternalObject( null, element.getType());
    ModelAlgorithms.copyAttributes( element, reference);

    boolean dirty = Xlate.get( annotation, "dirty", true);
    reference.setCachingPolicy( cachingPolicy);
    reference.setDirty( dirty);
    
    return reference;
  }
  
  /**
   * Expand all the annotations in the specified tree.
   * @param root The root.
   */
  private void expandAllAnnotations( IModelObject root)
  {
    for( IModelObject object: extendsExpr.query( root, null))
      expandAnnotation( object);
  }
  
  /**
   * Expand the specified annotation with inherited information if the <i>extends</i> attribute is present.
   * @param annotation The annotation to be expanded.
   */
  private void expandAnnotation( IModelObject annotation)
  {
    String extendSpec = Xlate.get( annotation, "extends", (String)null);
    if ( extendSpec == null) return;
    annotation.removeAttribute( "extends");    
    
    IContext context = new Context( parent, annotation);
    
    // find super annotation
    IExpression extendExpr = XPath.createExpression( extendSpec);
    IModelObject superAnnotation = extendExpr.queryFirst( context);
    superAnnotation = superAnnotation.cloneTree();
    
    // expand annotations in the super annotation
    expandAllAnnotations( superAnnotation);
    
    // diff and selective apply records to get inheritence
    XmlDiffer differ = new XmlDiffer();
    UnionChangeSet changeSet = new UnionChangeSet();
    differ.diff( annotation, superAnnotation, changeSet);
    for( IBoundChangeRecord record: changeSet.getRecords())
    {
      // ignore attribute changes for attributes that already exist
      IModelObject bound = record.getBoundObject();
      if ( record.isType( IChangeRecord.CHANGE_ATTRIBUTE))
      {
        String attrName = record.getAttributeName();
        if ( bound.getAttribute( attrName) != null) continue;
      }
      
      record.applyChange();
    }
  }
  
  /**
   * Create a ConfiguredCachingPolicy based on the specified annotation.
   * @param annotation The annotation.
   * @param staticStages The static stages (see ICachingPolicy).
   * @return Returns the ConfiguredCachingPolicy.
   */
  private ConfiguredCachingPolicy createCachingPolicy( IModelObject annotation, List<IModelObject> staticStages)
  {
    String policyClassName = Xlate.get( annotation, "class", (String)null);
    if ( policyClassName == null) return null;
    
    String cacheClassName = Xlate.get( annotation, "cache", "dunnagan.bob.xmodel.external.UnboundedCache");
    ICache cache = createCache( cacheClassName);
    
    ConfiguredCachingPolicy cachingPolicy = createCachingPolicy( cache, annotation, policyClassName);
    if ( cachingPolicy != null)
    {
      // configure
      cachingPolicy.configure( parent, annotation);
      
      // create dynamic next stages
      for( IModelObject stage: annotation.getChildren( "extern:match"))
      {
        IExpression stagePath = Xlate.get( stage, "path", (IExpression)null);
        boolean dirty = Xlate.get( stage, "dirty", true);
        ConfiguredCachingPolicy stageCachingPolicy = (ConfiguredCachingPolicy)stage.getAttribute( "extern:cp");
        
        // inherit parent caching policy if extern:match does not define a caching policy
        if ( stageCachingPolicy == null) stageCachingPolicy = cachingPolicy;
        
        // define stage and detach
        cachingPolicy.defineNextStage( stagePath, stageCachingPolicy, dirty);
        stage.removeFromParent();
      }
      
      // define static next stages
      for( IModelObject staticStage: staticStages)
        if ( !staticStage.isType( "extern:cache") && !staticStage.isType( "extern:match"))
          cachingPolicy.defineNextStage( staticStage);
      
      return cachingPolicy;
    }
    
    return null;
  }

  /**
   * Load the caching policy class.
   * @param cache The cache.
   * @param annotation The annotation.
   * @param className The optionally partial class name.
   */
  private ConfiguredCachingPolicy createCachingPolicy( ICache cache, IModelObject annotation, String className)
  {
    if ( !className.contains( "."))
    {
      IContext context = new Context( parent, annotation);
      List<IModelObject> packages = packageExpr.query( context, null);
      for( IModelObject pkg: packages)
      {
        String packageName = Xlate.get( pkg, "");
        ConfiguredCachingPolicy cachingPolicy = createCachingPolicy( cache, packageName, className);
        if ( cachingPolicy != null) return cachingPolicy;
      }
    }
    else
    {
      ConfiguredCachingPolicy cachingPolicy = createCachingPolicy( cache, (String)null, className);
      if ( cachingPolicy != null) return cachingPolicy;
    }
    
    return null;
  }
  
  /**
   * Load the caching policy class by concatenating the specified package name and class name.
   * @param cache The cache.
   * @param packageName The package name.
   * @param className The class name.
   */
  @SuppressWarnings("unchecked")
  private ConfiguredCachingPolicy createCachingPolicy( ICache cache, String packageName, String className)
  {
    if ( packageName != null) className = packageName+"."+className;
    try
    {
      Class policyClass = loader.loadClass( className);
      Constructor constructor = policyClass.getConstructor( new Class[] { ICache.class});
      Object[] params = new Object[] { cache};
      ConfiguredCachingPolicy cachingPolicy = (ConfiguredCachingPolicy)constructor.newInstance( params);
      cachingPolicy.setFactory( factory);
      return cachingPolicy;
    }
    catch( Exception e)
    {
      return null;
    }
  }

  /**
   * Create the specified ICache class.
   * @param className The class name of the ICache.
   */
  @SuppressWarnings("unchecked")
  private ICache createCache( String className)
  {
    try
    {
      Class<ICache> cacheClass = (Class<ICache>)loader.loadClass( className);
      return (ICache)cacheClass.newInstance();
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
      return null;
    }
  }
  
  private final IExpression packageExpr = XPath.createExpression(
    "ancestor-or-self::*/package");
  
  private final IExpression extendsExpr = XPath.createExpression(
    "descendant-or-self::extern:cache[ @extends]");
  
  private final IExpression annotatedExpr = XPath.createExpression(
    "reverse( descendant-or-self::*[ extern:cache])");
  
  private ClassLoader loader;
  private IContext parent;
  private IModelObjectFactory factory;
}
