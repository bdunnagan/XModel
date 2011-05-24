package org.xmodel.external.caching;

import java.util.HashMap;
import java.util.Map;

import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.ModelListener;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ExternalReference;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.NonSyncingIterator;
import org.xmodel.external.UnboundedCache;
import org.xmodel.xml.XmlIO;

/**
 * An implementation of ICachingPolicy that creates and maintains a copy of an IModelObject.  This
 * class is useful in multi-threaded applications where one thread needs to bind expressions for
 * elements managed by a different thread. 
 */
public class CopyCachingPolicy extends ConfiguredCachingPolicy
{
  public CopyCachingPolicy( IModelObject source)
  {
    this( source, new UnboundedCache());
  }
  
  public CopyCachingPolicy( IModelObject source, ICache cache)
  {
    super( cache);
    this.source = source;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    update( reference, copy( source, reference.getModel()));
  }
  
  /**
   * Create a copy of the specified elements putting IExternalReferences where there are dirty references.
   * @param root The element to be copied.
   * @param model The model to be assigned to the copy.
   * @return Returns the copy.
   */
  private IModelObject copy( IModelObject root, IModel model)
  {
    IModelObject result = null;
    Map<IModelObject, IModelObject> map = new HashMap<IModelObject, IModelObject>();
    NonSyncingIterator iter = new NonSyncingIterator( root);
    while( iter.hasNext())
    {
      IModelObject lNode = iter.next();
      IModelObject rParent = map.get( lNode.getParent());
      if ( lNode.isDirty())
      {
        ExternalReference rNode = new ExternalReference( lNode.getType());
        rNode.setCachingPolicy( new CopyCachingPolicy( lNode, getCache()));
        rNode.setDirty( true);
        if ( rParent != null) rParent.addChild( rNode);
        
        lNode.addModelListener( new Listener( rNode));
        
        map.put( lNode, rNode);
        if ( result == null) result = rNode;
      }
      else
      {
        IModelObject rNode = lNode.cloneObject();
        if ( rParent != null) rParent.addChild( rNode);
        
        lNode.addModelListener( new Listener( rNode));
        
        map.put( lNode, rNode);
        if ( result == null) result = rNode;
      }
    }    
    return result;
  }
  
  private class Listener extends ModelListener
  {
    public Listener( IModelObject rNode)
    {
      this.rNode = rNode;
    }
    
    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyAddChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
     */
    @Override
    public void notifyAddChild( IModelObject parent, IModelObject child, int index)
    {
      if ( rNode.isDirty()) return;
      rNode.addChild( copy( child), index);
    }

    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyRemoveChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
     */
    @Override
    public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
    {
      if ( rNode.isDirty()) return;
      rNode.removeChild( index);
    }

    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
    {
      if ( rNode.isDirty()) return;
      rNode.setAttribute( attrName, newValue);
    }

    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyClear(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
     */
    @Override
    public void notifyClear( IModelObject object, String attrName, Object oldValue)
    {
      if ( rNode.isDirty()) return;
      rNode.removeAttribute( attrName);
    }
    
    private IModelObject rNode;
  };
  
  private IModelObject source;
  
  public static void main( String[] args) throws Exception
  {
    String xml = "" +
    		"<lA>" +
        "  <lB/>" +
        "  <lB/>" +
        "  <lB/>" +
    		"</lA>";
    
    XmlIO xmlIO = new XmlIO();
    final IModelObject lA = xmlIO.read( xml);
    
    Thread thread = new Thread( new Runnable() {
      public void run()
      {
        ExternalReference rA = new ExternalReference( "rA");
        rA.setCachingPolicy( new CopyCachingPolicy( lA));
        rA.setDirty( true);
        rA.getChildren();
      }
    });
    thread.start();
    
    Thread.sleep( 1000);
    lA.getChild( 1).setAttribute( "name", "lB1");
  }
}
