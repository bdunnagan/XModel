package org.xmodel.mongo;
import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.ITransaction;
import org.xmodel.xpath.expression.IContext;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public abstract class AbstractMongoCachingPolicy extends ConfiguredCachingPolicy
{
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#configure(org.xmodel.xpath.expression.IContext, org.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    super.configure( context, annotation);
    createMongo( annotation);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.AbstractCachingPolicy#transaction()
   */
  @Override
  public ITransaction transaction()
  {
    return new MongoTransaction();
  }
  
  /**
   * Returns the collection for the specified reference.
   * @param reference The reference.
   * @return Returns the collection for the specified reference.
   */
  protected DBCollection getCollection( IExternalReference reference)
  {
    return getDB().getCollection( reference.getType());
  }

  private void createMongo( IModelObject annotation)
  {
    String host = Xlate.get( annotation, "host", Xlate.childGet( annotation, "host", "localhost"));
    int port = Xlate.get( annotation, "port", Xlate.childGet( annotation, "port", 27017));
    mongo = MongoAccess.getInstance( host, port);
    
    String dbName = Xlate.get( annotation, "db", Xlate.childGet( annotation, "db", (String)null));
    if ( dbName == null) 
      throw new CachingException( 
        String.format( "Mongo DB is not defined at %s", 
        ModelAlgorithms.createIdentityPath( annotation)));

    db = mongo.getDB( dbName);
  }
  
  /**
   * @return Returns the Mongo instance.
   */
  protected Mongo getMongo()
  {
    return mongo;
  }
  
  /**
   * @return Returns the Mongo DB instance.
   */
  protected DB getDB()
  {
    return db;
  }
  
  /**
   * @return Returns the Mongo <-> IModelObject transform implementation.
   */
  protected IMongoTransform getTransform()
  {
    return transform;
  }
  
  private Mongo mongo;
  private DB db;
  private IMongoTransform transform;
}
