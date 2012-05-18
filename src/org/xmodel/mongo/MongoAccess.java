package org.xmodel.mongo;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.xmodel.log.SLog;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

public class MongoAccess
{
  /**
   * Returns a Mongo instance for the specified address.
   * @param address The address of the database.
   * @return Returns null or a Mongo instance for the specified address.
   */
  public static synchronized Mongo getInstance( String host, int port)
  {
    try
    {
      return getInstance( new ServerAddress( host, port));
    }
    catch( UnknownHostException e)
    {
      SLog.warn( MongoAccess.class, e.getMessage());
      return null;
    }
  }
  
  /**
   * Returns a Mongo instance for the specified address.
   * @param address The address of the database.
   * @return Returns a Mongo instance for the specified address.
   */
  private static synchronized Mongo getInstance( ServerAddress address)
  {
    Mongo mongo = mongos.get( address);
    if ( mongo == null)
    {
      mongo = new Mongo( address);
      mongos.put( address, mongo);
    }
    return mongo;
  }
  
  private static Map<ServerAddress, Mongo> mongos = new HashMap<ServerAddress, Mongo>();
}
