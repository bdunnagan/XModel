/*
 * Stonewall Networks, Inc.
 *
 *   Project: XModel
 *   Author:  bdunnagan
 *   Date:    Mar 12, 2008
 *
 * Copyright 2008.  Stonewall Networks, Inc.
 */
package dunnagan.bob.xmodel.xaction;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelAlgorithms;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.external.*;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;
import dunnagan.bob.xmodel.xpath.variable.IVariableScope;

/**
 * An XAction which creates one or more ExternalReferences and assigns instances of
 * ConfiguredCachingPolicy to tem. The caching policies are configured from the annotation defined
 * in the action. The new ExternalReferences are optionally marked dirty.
 */
public class CreateExternalAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#configure(dunnagan.bob.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    assign = Xlate.get( document.getRoot(), "assign", (String)null);
    parentExpr = document.getExpression( "parent", false);
    sourceExpr = document.getExpression( "source", false);
    classExpr = document.getExpression( "class", false);
    mode = Xlate.get( document.getRoot(), "mode", "share");
    dirty = Xlate.get( document.getRoot(), "dirty", true);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    // create annotation from subset of document
    IModelObject annotation = getDocument().getRoot().cloneTree();
    annotation.removeChildren( "source");
    annotation.removeChildren( "class");
    annotation.removeAttribute( "mode");
    annotation.removeAttribute( "dirty");
    
    ICachingPolicy cachingPolicy = mode.equals( "share")? createCachingPolicy( context, annotation): null;
    
    // get parent
    List<IModelObject> parents = parentExpr.query( context, null);
    
    // create references
    List<ExternalReference> references = new ArrayList<ExternalReference>();
    List<IModelObject> elements = sourceExpr.query( context, null);
    for( IModelObject element: elements)
    {
      ExternalReference reference = new ExternalReference( element.getType());
      ModelAlgorithms.copyAttributes( element, reference);
      if ( mode.equals( "unique")) cachingPolicy = createCachingPolicy( context, annotation);
      references.add( reference);
    }

    // set dirty after creating everything
    for( ExternalReference reference: references)
      reference.setCachingPolicy( cachingPolicy, dirty);

    // add references to parents
    for( ExternalReference reference: references)
      for( IModelObject parent: parents)
        parent.addChild( reference);
    
    // assign to variable
    if ( assign != null)
    {
      IVariableScope scope = context.getScope();
      List<IModelObject> list = new ArrayList<IModelObject>( references.size());
      list.addAll( references);
      if ( scope != null) scope.set( assign, list);
    }
  }

  /**
   * Create an instance of the caching policy class.
   * @param context The context.
   * @param annotation The caching policy annotation.
   * @return Returns the instance or null.
   */
  private ICachingPolicy createCachingPolicy( IContext context, IModelObject annotation)
  {
    String className = classExpr.evaluateString( context);
    try
    {
      ClassLoader loader = getDocument().getClassLoader();
      Class<ConfiguredCachingPolicy> clss = (Class<ConfiguredCachingPolicy>)loader.loadClass( className);
      Constructor<ConfiguredCachingPolicy> constructor = clss.getConstructor( new Class[] { ICache.class});
      ConfiguredCachingPolicy cachingPolicy = constructor.newInstance( new Object[] { new UnboundedCache()});
      cachingPolicy.configure( context, annotation);
      return cachingPolicy;
    }
    catch( Exception e)
    {
      System.err.println( "Unable to load caching policy class: "+className);
      e.printStackTrace( System.err);
      return null;
    }
  }
  
  /**
   * Returns the package name portion of the class name.
   * @param className The class name.
   * @return Returns the package name portion of the class name.
   */
  private String getPackageName( String className)
  {
    int index = className.lastIndexOf( '.');
    return className.substring( 0, index);
  }
  
  private String assign;
  private IExpression parentExpr;
  private IExpression sourceExpr;
  private IExpression classExpr;
  private String mode;
  private boolean dirty;
}
