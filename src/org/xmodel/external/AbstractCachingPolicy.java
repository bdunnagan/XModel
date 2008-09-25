/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.external;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelListenerList;
import org.xmodel.ModelObjectFactory;
import org.xmodel.ModelRegistry;
import org.xmodel.diff.IXmlDiffer;
import org.xmodel.diff.XmlDiffer;
import org.xmodel.xml.IXmlIO;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.IExpression;


/**
 * A base implementation of ICachingPolicy which handles all the semantics except 
 * those pertaining to synchronizing references with their backing store.
 */
public abstract class AbstractCachingPolicy implements ICachingPolicy
{
  /**
   * Create an AbstractCachingPolicy which keeps everything in memory.
   */
  protected AbstractCachingPolicy()
  {
    this( new UnboundedCache());
  }
  
  /**
   * Create an AbstractCachingPolicy which uses the specified cache.
   * @param cache The cache.
   */
  protected AbstractCachingPolicy( ICache cache)
  {
    this.cache = cache;
    xmlIO = new XmlIO();
    differ = new XmlDiffer();
    staticAttributes = new String[] { "id"};
    factory = new ModelObjectFactory();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#getCache()
   */
  public ICache getCache()
  {
    return cache;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#setFactory(org.xmodel.IModelObjectFactory)
   */
  public void setFactory( IModelObjectFactory factory)
  {
    this.factory = factory;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#getFactory()
   */
  public IModelObjectFactory getFactory()
  {
    return factory;
  }

  /**
   * Returns the IXmlDiffer used by this caching policy.
   * @return Returns the IXmlDiffer used by this caching policy.
   */
  public IXmlDiffer getDiffer()
  {
    return differ;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#defineNextStage(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.external.ICachingPolicy, boolean)
   */
  public void defineNextStage( IExpression path, ICachingPolicy cachingPolicy, boolean dirty)
  {
    NextStage stage = new NextStage();
    stage.path = path;
    stage.cachingPolicy = cachingPolicy;
    stage.dirty = dirty;
    
    if ( dynamicStages == null) dynamicStages = new ArrayList<NextStage>( 1);
    dynamicStages.add( stage);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#defineNextStage(org.xmodel.IModelObject)
   */
  public void defineNextStage( IModelObject stage)
  {
    if ( staticStages == null) staticStages = new ArrayList<IModelObject>( 1);
    staticStages.add( stage);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#createExternalTree(org.xmodel.IModelObject, boolean, org.xmodel.external.IExternalReference)
   */
  public IExternalReference createExternalTree( IModelObject local, boolean dirty, IExternalReference proto)
  {
    IExternalReference external = (IExternalReference)proto.createObject( local.getType());
    if ( dirty)
    {
      // copy static attributes to reference which will become dirty
      for( String attrName: getStaticAttributes())
        if ( !attrName.contains( "*"))
          external.setAttribute( attrName, local.getAttribute( attrName));
    }
    else
    {
      // copy all attributes and move children
      ModelAlgorithms.copyAttributes( local, external);
      ModelAlgorithms.moveChildren( local, external);
      
      // apply next stages
      applyNextStages( external, proto);
    }
    
    external.setCachingPolicy( this);
    external.setDirty( dirty);
    return external;
  }

  /**
   * Apply the next stages to the specified subtree. This method transforms objects in the subtree
   * which should be, but are not yet, external references according to the next stages defined on
   * this caching policy.
   * @param object The object to which next stages will be applied.
   * @param proto The prototype for external references.
   */
  protected void applyNextStages( IModelObject object, IExternalReference proto)
  {
    Context context = new Context( object);
    
    // apply dynamic stages
    if ( dynamicStages != null)
    {
      for( NextStage stage: dynamicStages)
      {
        List<IModelObject> matches = stage.path.evaluateNodes( context);
        for( IModelObject matched: matches)
        {
          IExternalReference replacement = stage.cachingPolicy.createExternalTree( matched, stage.dirty, proto);
          ModelAlgorithms.substitute( matched, replacement);
        }
      }
    }
    
    // apply static stages
    if ( staticStages != null)
    {
      for( IModelObject stage: staticStages)
      {
        IModelObject clone = ModelAlgorithms.cloneExternalTree( stage, null);
        object.addChild( clone);
      }
    }
  }
  
  /**
   * Set the dirty state of the dynamic next stages.
   * @param reference The reference.
   */
  protected void markNextStages( IExternalReference reference)
  {
    if ( dynamicStages == null) return;
    
    Context context = new Context( reference);
    for( NextStage stage: dynamicStages)
    {
      if ( stage.dirty)
      {
        List<IModelObject> matches = stage.path.evaluateNodes( context);
        for( IModelObject matched: matches)
        {
          // stage may be dirty since diff was performed with sync turned off (see update)
          ((IExternalReference)matched).setDirty( false);
          
          // capture location of next stage
          IModelObject parent = matched.getParent();
          int index = parent.getChildren().indexOf( matched);
          
          // remove from parent
          matched.removeFromParent();
          
          // mark dirty
//System.out.println( "Mark dirty: "+matched+", children? "+matched.getNumberOfChildren());          
          ((IExternalReference)matched).setDirty( true);
          
          // add back to parent
          parent.addChild( matched, index);
        }
      }
    }
  }
  
  /**
   * Mark the static and dynamic next stages not-dirty.
   * @param reference The reference.
   */
  protected void markCleanNextStages( IExternalReference reference)
  {
    NonSyncingIterator iter = new NonSyncingIterator( reference);
    while( iter.hasNext())
    {
      IModelObject element = iter.next();
      if ( element.isDirty()) ((IExternalReference)element).setDirty( false);
    }
  }

  /**
   * This default implementation does not performing locking.
   */
  public void checkin( IExternalReference reference)
  {
  }

  /**
   * This default implementation does not performing locking.
   */
  public void checkout( IExternalReference reference)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#clear(org.xmodel.external.IExternalReference)
   */
  public void clear( IExternalReference reference) throws CachingException
  {
    if ( reference.isDirty()) return;
    
    // mark all next stages not-dirty
    markCleanNextStages( reference);
    
    // resync immediately if reference has listeners
    ModelListenerList listeners = reference.getModelListeners();
    if ( listeners != null && listeners.count() > 0)
    {
      // HACK: this is only necessary until proper reference diffing is implemented
      reference.removeChildren();
      
//System.out.println( "Resyncing: "+reference);
      // sync
      sync( reference);
      
      // mark next stages dirty (only necessary if children are not removed - see above)
      //markNextStages( reference);
    }
    
    // remove children and set reference dirty
    else
    {
//System.out.println( "Clearing: "+reference);
      
      // removing children here is a fundamental semantic and has two major interactions:
      //   1. It enables the ICache to manage the space in the cache using this method.
      //   2. It causes FanoutListener to remove its listeners.
      //
      reference.removeChildren();

      // set dirty
      reference.setDirty( true);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#flush(org.xmodel.external.IExternalReference)
   */
  public void flush( IExternalReference reference) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#insert(org.xmodel.external.IExternalReference, 
   * java.lang.String, boolean)
   */
  public void insert( IExternalReference parent, String xml, int index, boolean dirty) throws CachingException
  {
    try
    {
      insert( parent, xmlIO.read( xml), -1, dirty);
    }
    catch( XmlException e)
    {
      throw new CachingException( "Unable to insert entity: "+xml, e);
    } 
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#remove(org.xmodel.external.IExternalReference, 
   * java.lang.String)
   */
  public void remove( IExternalReference parent, String xml) throws CachingException
  {
    try
    {
      remove( parent, xmlIO.read( xml));
    }
    catch( XmlException e)
    {
      throw new CachingException( "Unable to remove entity: "+xml, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#update(org.xmodel.external.IExternalReference, 
   * java.lang.String)
   */
  public void update( IExternalReference reference, String xml) throws CachingException
  {
    try
    {
      update( reference, xmlIO.read( xml));
    }
    catch( XmlException e)
    {
      throw new CachingException( "Unable to update entity: "+xml, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#insert(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject, int, boolean)
   */
  public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException
  {
    // must insert object after applying next stages so create a clone of parent
    IModelObject parentClone = parent.cloneObject();
    parentClone.addChild( object);
    applyNextStages( parentClone, parent);
    
    // mark child dirty
    IModelObject child = parentClone.getChild( 0);
    if ( child instanceof IExternalReference) ((IExternalReference)child).setDirty( dirty);
    
    // move child from clone to parent
    if ( index >= 0)
    {
      parent.addChild( child, index);
    }
    else
    {
      parent.addChild( child);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#update(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject)
   */
  public void update( IExternalReference reference, IModelObject object) throws CachingException
  {
    // create next stages on prototype object
    applyNextStages( object, reference);
    
    // turn off syncing while updating reference
    boolean syncLock = reference.getModel().getSyncLock();
    try
    {
      reference.getModel().setSyncLock( true);
      differ.diffAndApply( reference, object);
      
      // This is necessary when children have not been removed by the clear method
      //markNextStages( reference);
    }
    finally
    {
      reference.getModel().setSyncLock( syncLock);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#remove(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
    IModelObject matched = ModelAlgorithms.findFastSimpleMatch( parent.getChildren(), object);
    if ( matched != null) matched.removeFromParent();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#getStaticAttributes()
   */
  public String[] getStaticAttributes()
  {
    return staticAttributes;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#readAttributeAccess(
   * org.xmodel.external.IExternalReference, java.lang.String)
   */
  public void readAttributeAccess( IExternalReference reference, String attrName)
  {
    if ( !isStaticAttribute( attrName)) 
    {
      if ( cache != null) cache.touch( reference);
      if ( reference.isDirty()) internal_sync( reference);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#readChildrenAccess(
   * org.xmodel.external.IExternalReference)
   */
  public void readChildrenAccess( IExternalReference reference)
  {
    if ( cache != null) cache.touch( reference);
    if ( reference.isDirty()) internal_sync( reference);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#writeAttributeAccess(
   * org.xmodel.external.IExternalReference, java.lang.String)
   */
  public void writeAttributeAccess( IExternalReference reference, String attrName)
  {
    if ( !isStaticAttribute( attrName)) 
    {
      if ( cache != null) cache.touch( reference);
      if ( reference.isDirty()) internal_sync( reference);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#writeChildrenAccess(
   * org.xmodel.external.IExternalReference)
   */
  public void writeChildrenAccess( IExternalReference reference)
  {
    if ( cache != null) cache.touch( reference);
    if ( reference.isDirty()) internal_sync( reference);
  }
  
  /**
   * Specify the names of attributes which should not cause synchronization. Two types of 
   * wildcards can be used. An asterisk by itself means <i>all attributes</i>. A prefix 
   * ending with a colon followed by an asterisk means <i>all attributes in namespace</i>.
   * @param staticAttributes An array of attribute names.
   */
  protected void setStaticAttributes( String[] staticAttributes)
  {
    this.staticAttributes = staticAttributes;
  }
  
  /**
   * Returns true if the specified attribute is in the list of static attributes.
   * @param attribute The attribute to be tested.
   * @return Returns true if the specified attribute is in the list of static attributes.
   */
  public boolean isStaticAttribute( String attribute)
  {
    if ( attribute == null)
    {
      return staticAttributes.length > 0 && staticAttributes[ 0].equals( "*");
    }
    else
    {
      for( int i=0; i<staticAttributes.length; i++)
      {
        String staticAttribute = staticAttributes[ i];
        if ( staticAttribute.endsWith( ":*"))
        {
          String prefix = staticAttribute.substring( 0, staticAttribute.length()-2);
          if ( attribute.startsWith( prefix)) return true;
        }
        else if ( staticAttribute.equals( "*") || attribute.equals( staticAttribute))
        {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * This method conditions the reference before performing the sync. 
   * The dirty flag is cleared and the reference is added to the cache.
   * @param reference The reference to be synced.
   */
  protected void internal_sync( IExternalReference reference) throws CachingException
  {
    // check sync lock before proceeding
    IModel model = reference.getModel();
    if ( model.getSyncLock()) return;
    
    // clear dirty flag
    reference.setDirty( false);
    
    // unlock reference so that the reference can be updated in the stack frame of the
    // notification for its being inserted into the model dirty
    boolean wasLocked = (model.isLocked( reference) != null);
    model.unlock( reference);
    
    try
    {
      // sync
      sync( reference);
    }
    finally
    {
      // reference enters cache when it is first synced
      if ( cache != null) cache.add( reference);
      
      // relock reference if it was previously locked
      if ( wasLocked) model.lock( reference);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#toString(java.lang.String)
   */
  public String toString( String indent)
  {
    IModel model = ModelRegistry.getInstance().getModel();
    boolean syncLock = model.getSyncLock();
    model.setSyncLock( true);
    try
    {
      StringBuilder sb = new StringBuilder();
      
      if ( traversed)
      {
        sb.append( indent); sb.append( getClass().getSimpleName()); sb.append( "\n");
        return sb.toString();
      }
      traversed = true;

      // add class name
      sb.append( indent); sb.append( getClass().getSimpleName());
      if ( (dynamicStages != null && dynamicStages.size() > 0) || (staticStages != null && staticStages.size() > 0))
        sb.append( "\n"); sb.append( indent); sb.append( "{\n");
  
      String nextIndent = indent + "  ";
      
      // add stages
      if ( dynamicStages != null)
        for( NextStage stage: dynamicStages)
        {
          sb.append( nextIndent); sb.append( stage.dirty); sb.append( ", "); sb.append( stage.path); sb.append( "\n");
          if ( stage.cachingPolicy != null) sb.append( stage.cachingPolicy.toString( nextIndent));
        }
      
      // add static stages
      XmlIO xmlIO = new XmlIO() {
        protected void output( int indent, IModelObject root, OutputStream stream) throws IOException
        {
          super.output( indent, root, stream);
          
          StringBuilder sb = new StringBuilder();
          for( int i=0; i<indent; i++) sb.append( ' ');
          sb.append( "# ");
          if ( root instanceof IExternalReference)
          {
            ICachingPolicy cachingPolicy = ((IExternalReference)root).getCachingPolicy();
            stream.write( cachingPolicy.toString( sb.toString()).getBytes());
          }
        }
      };
      
      if ( staticStages != null)
        for( IModelObject stage: staticStages)
        {
          sb.append( xmlIO.write( indent.length()+2, stage));
          sb.append( "\n");
        }
      
      if ( (dynamicStages != null && dynamicStages.size() > 0) || (staticStages != null && staticStages.size() > 0))
        sb.append( indent); sb.append( "}\n");
        
      return sb.toString();
    }
    finally
    {
      traversed = false;
      model.setSyncLock( syncLock);
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return toString( "");
  }

  /**
   * A bag to hold the definition of a dynamic next-stage.
   */
  private class NextStage
  {
    IExpression path;
    ICachingPolicy cachingPolicy;
    boolean dirty;
  }
  
  private ICache cache;
  private IXmlIO xmlIO;
  private IXmlDiffer differ;
  private String[] staticAttributes;
  private List<NextStage> dynamicStages;
  private List<IModelObject> staticStages;
  private IModelObjectFactory factory;
  private boolean traversed; // debug
}
