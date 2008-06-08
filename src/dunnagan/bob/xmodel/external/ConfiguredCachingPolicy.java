/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IPath;
import dunnagan.bob.xmodel.ModelAlgorithms;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.xml.XmlIO;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/** 
 * A further refinement of AbstractCachingPolicy which provides built-in support for secondary caching
 * stages and configuration from metadata annotations culled during preprocessing of a model loaded 
 * from an xml file. This class also supports the definition of a skeleton tree which is automatically
 * created when an external reference is synchronized so that secondary stages are immediately available.
 * This architecture supports resolution of an external reference in three ways.
 * 
 * <ul>
 * <li>The reference consists solely of information from the external storage location.
 * <li>The reference consists of a skeleton with zero or more secondary stages.
 * <li>The reference is a combination of the previous two methods.
 * </ul>
 * 
 * The subclass implementation of the <code>sync</code> method is responsible for applying the skeleton
 * structure. The relative position of the skeleton with respect to elements constructed from information
 * in the external storage location is implementation dependent.
 * <p>
 * <b>Most implementations of ICachingPolicy should use this base class instead of AbstractCachingPolicy.</b>
 * <p>
 * The schema and examples of the metadata annotation are provided in the metadata.xsd and metadata.xml
 * files in this package. Within the metadata annotation is an arbitrary fragment which is passed directly
 * to the caching policy and contains caching policy specific configuration.
 */
public abstract class ConfiguredCachingPolicy extends AbstractCachingPolicy
{
  protected ConfiguredCachingPolicy( ICache cache)
  {
    super( cache);
    differ = new EntityDiffer();
  }

  /**
   * Configure this caching policy from the specified annotation.
   * @param context The context in which to evaluate expressions in the annotation.
   * @param annotation The annotation element.
   */
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    // static attributes
    String list = Xlate.childGet( annotation, "static", (String)null);
    if ( list != null)
    {
      String[] staticAttributes = list.split( ",");
      for( int i=0; i<staticAttributes.length; i++)
        staticAttributes[ i] = staticAttributes[ i].trim();
      setStaticAttributes( staticAttributes);
    }
  }
  
  /**
   * Defines the skeleton to be applied to the reference when the reference is synchronized. The position of
   * elements defined in the skeleton tree relative to elements constructed from information in the external
   * storage location is implementation dependent. The skeleton tree is stored by reference.
   * @param skeleton The default skeleton tree.
   */
  public void defineSkeleton( IModelObject skeleton)
  {
    this.skeleton = skeleton;
  }
  
  /**
   * Define a secondary stage for elements on the specified relative path and install the specified
   * caching policy. If the dirty argument is true then ExternalReference instances for the stage
   * will be created in the dirty state.
   * @param path The relative path of the secondary stage.
   * @param cachingPolicy The caching policy to install.
   * @param dirty True if ExternalReference instances should be created dirty.
   * @deprecated
   */
  public void defineSecondaryStage( IPath path, ConfiguredCachingPolicy cachingPolicy, boolean dirty)
  {
    differ.addCachingPolicy( path, cachingPolicy, dirty);
  }
  
  /**
   * Define a secondary stage for elements on the specified relative path and install the specified
   * caching policy. If the dirty argument is true then ExternalReference instances for the stage
   * will be created in the dirty state.
   * @param path The relative path of the secondary stage.
   * @param cachingPolicy The caching policy to install.
   * @param dirty True if ExternalReference instances should be created dirty.
   */
  public void defineSecondaryStage( IExpression path, ConfiguredCachingPolicy cachingPolicy, boolean dirty)
  {
    differ.addCachingPolicy( path, cachingPolicy, dirty);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#prepare(dunnagan.bob.xmodel.external.IExternalReference, boolean)
   */
  public void prepare( IExternalReference reference, boolean dirty)
  {
    super.prepare( reference, dirty);
    if ( !dirty) applySkeleton( reference);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#sync(dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void sync( IExternalReference reference) throws CachingException
  {
    syncImpl( reference);
    applySkeleton( reference);
  }

  /**
   * Called to synchronize the reference.
   * @param reference The reference.
   */
  protected abstract void syncImpl( IExternalReference reference) throws CachingException;
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.AbstractCachingPolicy#insert(dunnagan.bob.xmodel.external.IExternalReference, 
   * dunnagan.bob.xmodel.IModelObject, int, boolean)
   */
  public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException
  {
    if ( parent.isDirty()) return;
    
    // transform object into external reference
    IModelObject dummy = parent.cloneObject();
    dummy.addChild( object);
    differ.applyTransform( dummy);
    
    // reset dirty flag
    object = dummy.getChild( 0);
    if ( object instanceof IExternalReference)
      ((IExternalReference)object).setDirty( dirty);

    // handle index == -1
    if ( index < 0) index = parent.getNumberOfChildren();
    
    // add transformed to parent
    parent.addChild( object, index);
    
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.AbstractCachingPolicy#remove(dunnagan.bob.xmodel.external.IExternalReference, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
    if ( parent.isDirty()) return;
    IModelObject child = parent.getChild( object.getType(), object.getID());
    if ( child != null) child.removeFromParent();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#update(dunnagan.bob.xmodel.external.IExternalReference, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  public void update( IExternalReference reference, IModelObject object) throws CachingException
  {
    if ( reference.isDirty()) return;
    differ.diffAndApply( reference, object);
  }
  
  /**
   * Apply the skeleton defined on this caching policy to the specified object if the object is not dirty.
   * @param object The object.
   */
  protected void applySkeleton( IModelObject object)
  {
    assert( !object.isDirty());

    if ( skeleton == null) return;
    
    // clone children of skeleton to reference
    if ( object.getNumberOfChildren() == 0)
    {
      for( IModelObject child: skeleton.getChildren())
        object.addChild( ModelAlgorithms.cloneExternalTree( child, null));
    }
  }

  /**
   * Returns the skeleton tree.
   * @return Returns the skeleton tree.
   */
  protected IModelObject getSkeleton()
  {
    return skeleton;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    XmlIO xmlIO = new XmlIO();
    StringBuilder sb = new StringBuilder();
    
    // show stages
    sb.append( "Stages: \n");
    sb.append( differ.toString());
    sb.append( "\n");
    
    // show skeleton without syncing
    sb.append( "Skeleton: \n");
    skeleton.getModel().setSyncLock( true);
    sb.append( xmlIO.write( skeleton));
    skeleton.getModel().setSyncLock( false);
    return sb.toString();
  }

  protected EntityDiffer differ;
  private IModelObject skeleton;
}
