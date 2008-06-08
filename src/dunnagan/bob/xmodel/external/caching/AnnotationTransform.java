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
import java.util.Stack;

import dunnagan.bob.xmodel.*;
import dunnagan.bob.xmodel.diff.XmlDiffer;
import dunnagan.bob.xmodel.external.ConfiguredCachingPolicy;
import dunnagan.bob.xmodel.external.ExternalReference;
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
    IExternalReference result = null;
   
    // expand annotations
    expandAllAnnotations( root);
    
    // manually iterate tree to optimize branch selection
    Stack<IModelObject> stack = new Stack<IModelObject>();
    stack.push( root);
    while( !stack.empty())
    {
      IModelObject element = stack.pop();
      IModelObject annotation = element.getFirstChild( "extern:cache");
      if ( annotation != null && !annotation.getParent().isType( "extern:match"))
      {
        IExternalReference reference = transform( element, annotation);

        // replace element with new reference
        IModelObject parent = element.getParent();
        if ( parent != null)
        {
          int index = parent.getChildren().indexOf( element);
          element.removeFromParent();
          parent.addChild( reference, index);
        }
        
        // replace root
        if ( element == root) result = reference;
      }
      
      // annotation branch has already been removed
      for( IModelObject child: element.getChildren())
        stack.push( child);
    }
    
    return (result != null)? result: root;
  }
  
  /**
   * Transform the specified element into an IExternalReference based on the specified annotation.
   * @param element The element to be transformed.
   * @param annotation The annotation element.
   * @return Returns the new reference.
   */
  private IExternalReference transform( IModelObject element, IModelObject annotation)
  {
    ExternalReference reference = new ExternalReference( element.getType());
    ModelAlgorithms.copyAttributes( element, reference);

    // get secondary stages (match specifications)
    List<IModelObject> externMatches = annotation.getChildren( "extern:match");
    
    // create caching policy
    ConfiguredCachingPolicy cachingPolicy = createCachingPolicy( annotation);
    if ( cachingPolicy == null) return null;
    
    // define secondary stages
    for( IModelObject externMatch: externMatches)
    {
      IModelObject matchAnnotation = externMatch.getFirstChild( "extern:cache");
      IExpression path = XPath.createExpression( Xlate.get( externMatch, "path", (String)null));
      ConfiguredCachingPolicy matchedPolicy = createCachingPolicy( matchAnnotation);
      
      // support nesting
      if ( matchedPolicy == null) matchedPolicy = cachingPolicy;
      
      // define
      cachingPolicy.defineSecondaryStage( path, matchedPolicy, Xlate.get( externMatch, "dirty", true));

      // add skeleton to matched caching policy
      if ( matchedPolicy != cachingPolicy)
      {
        externMatch.removeFromParent();
        matchAnnotation.removeFromParent();
        matchedPolicy.defineSkeleton( transform( externMatch));
      }
    }
    
    // define skeleton
    annotation.removeFromParent();
    cachingPolicy.defineSkeleton( transform( element));
    
    // associate caching policy
    boolean dirty = Xlate.get( annotation, "dirty", true);
    reference.setCachingPolicy( cachingPolicy, dirty);
    
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
   * @return Returns the ConfiguredCachingPolicy.
   */
  private ConfiguredCachingPolicy createCachingPolicy( IModelObject annotation)
  {
    String policyClassName = Xlate.get( annotation, "class", (String)null);
    if ( policyClassName == null) return null;
    
    String cacheClassName = Xlate.get( annotation, "cache", "dunnagan.bob.xmodel.external.UnboundedCache");
    ICache cache = createCache( cacheClassName);
    
    ConfiguredCachingPolicy cachingPolicy = createCachingPolicy( cache, annotation, policyClassName);
    if ( cachingPolicy != null)
    {
      cachingPolicy.configure( parent, annotation);
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
  
  private ClassLoader loader;
  private IContext parent;
}
