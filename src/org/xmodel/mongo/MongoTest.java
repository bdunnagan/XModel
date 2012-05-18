package org.xmodel.mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class MongoTest
{
  public static void main( String[] args) throws Exception
  {
    Mongo mongo = new Mongo( "127.0.0.1" );
    DB db = mongo.getDB( "mydb");
    DBCollection collection = db.getCollection( "stuff");
  }
}
