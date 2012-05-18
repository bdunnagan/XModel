package org.xmodel.mongo;
import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.external.CachingException;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.ITransaction;
import com.mongodb.DBCursor;

public class MongoCachingPolicy extends AbstractMongoCachingPolicy
{
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    IMongoTransform transform = getTransform();
    DBCursor cursor = getCollection( reference).find( transform.getEntityReference( reference));
    
    if ( cursor.count() == 0)
    {
      throw new CachingException( String.format(
          "Entity not found in database: %s", 
          ModelAlgorithms.createIdentityPath( reference)));
    }
    
    if ( cursor.count() > 1)
    {
      throw new CachingException( String.format(
          "Entity is ambiguous in database: %s", 
          ModelAlgorithms.createIdentityPath( reference)));
    }
    
    IModelObject element = transform.getElement( cursor.next());
    if ( element == null)
    {
      throw new CachingException( String.format(
          "Unable to transform database entity: %s", 
          ModelAlgorithms.createIdentityPath( reference)));
    }
    
    update( reference, element);
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
