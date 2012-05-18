package org.xmodel.mongo;
import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.external.CachingException;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.ITransaction;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoListCachingPolicy extends AbstractMongoCachingPolicy
{
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    IMongoTransform transform = getTransform();
    DBCursor cursor = getCollection( reference).find( transform.getEntityReference( reference));

    IModelObject parent = reference.cloneObject();
    for( DBObject object: cursor)
    {
      IModelObject element = transform.getElement( object);
      if ( element == null)
      {
        throw new CachingException( String.format(
            "Unable to transform database entity: %s", 
            ModelAlgorithms.createIdentityPath( reference)));
      }
      
      parent.addChild( element);
    }
    
    update( reference, parent);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.AbstractCachingPolicy#transaction()
   */
  @Override
  public ITransaction transaction()
  {
    return new MongoTransaction();
  }
}
