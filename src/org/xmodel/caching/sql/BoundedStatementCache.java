package org.xmodel.caching.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xmodel.log.Log;
import org.xmodel.util.ThreadLocalMap;

/**
 * A bounded cache for PreparedStatement instances.
 */
public class BoundedStatementCache extends ThreadLocalMap<String, PreparedStatement>
{
  public BoundedStatementCache( int maxSize)
  {
    this.maxSize = maxSize;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.util.ThreadLocalMap#createThreadMap()
   */
  @SuppressWarnings("serial")
  @Override
  protected Map<String, PreparedStatement> createThreadMap()
  {
    return new LinkedHashMap<String, PreparedStatement>( 16, 0.75f, true) {
      protected boolean removeEldestEntry( Entry<String, PreparedStatement> eldest)
      {
        if ( size() >= maxSize)
        {
          try
          {
            log.debugf( "Removing statement from cache with key: %s", eldest.getKey());
            eldest.getValue().close();
          }
          catch( SQLException e)
          {
            log.warn( e.toString());
          }
          
          return true;
        }
        
        return false;
      }
    };
  }
  
  public final static Log log = Log.getLog( BoundedStatementCache.class);
  
  private int maxSize;
}
