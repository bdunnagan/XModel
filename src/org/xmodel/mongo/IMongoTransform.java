package org.xmodel.mongo;
import org.xmodel.IModelObject;
import com.mongodb.DBObject;

public interface IMongoTransform
{
  /**
   * Returns a DBObject reference to the specified database element.
   * @param element The element.
   * @return Returns a DBObject reference to the specified database element.
   */
  public DBObject getEntityReference( IModelObject element);
  
  /**
   * Returns a DBObject representation of the specified element.
   * @param element The element.
   * @return Returns a DBObject representation of the specified element.
   */
  public DBObject getEntity( IModelObject element);
  
  /**
   * Returns an element representing the specified entity.
   * @param entity The entity.
   * @return Returns an element representing the specified entity.
   */
  public IModelObject getElement( DBObject entity);
}
