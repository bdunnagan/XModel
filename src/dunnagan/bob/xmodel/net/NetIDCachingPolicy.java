package dunnagan.bob.xmodel.net;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelAlgorithms;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.external.CachingException;
import dunnagan.bob.xmodel.external.ConfiguredCachingPolicy;
import dunnagan.bob.xmodel.external.ICache;
import dunnagan.bob.xmodel.external.IExternalReference;

/**
 * An ICachingPolicy which resolves itself using a ModelClient instance.
 */
public class NetIDCachingPolicy extends ConfiguredCachingPolicy
{
  public NetIDCachingPolicy( ICache cache, ModelClient client)
  {
    super( cache);
    this.client = client;
    setStaticAttributes( new String[] { "id", "xm:*", "net:*"});
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ConfiguredCachingPolicy#syncImpl(dunnagan.bob.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    reference.removeChildren();
    
    try
    {
      // bind
      IModelObject result = client.sync( reference);
      if ( result == null) throw new CachingException( "Network operation timed out.");

      // update reference
      ModelAlgorithms.copyAttributes( result, reference);
      IModelObject[] array = result.getChildren().toArray( new IModelObject[ 0]);
      for( IModelObject child: array) reference.addChild( child);
    }
    catch( TimeoutException e)
    {
      throw new CachingException( "Unable to bind reference: "+reference, e);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.AbstractCachingPolicy#clear(dunnagan.bob.xmodel.external.IExternalReference)
   */
  @Override
  public void clear( IExternalReference reference) throws CachingException
  {
    // send clear message
    try
    {
      String netID = Xlate.get( reference, "net:id", "");
      assert( netID.length() > 0);
      client.unbind( netID);
    }
    catch( TimeoutException e)
    {
    }
    
    // clear
    super.clear( reference);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#flush(dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void flush( IExternalReference reference) throws CachingException
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#insert(dunnagan.bob.xmodel.external.IExternalReference, 
   * dunnagan.bob.xmodel.IModelObject, int, boolean)
   */
  public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#remove(dunnagan.bob.xmodel.external.IExternalReference, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
  }
  
  private ModelClient client;
}
