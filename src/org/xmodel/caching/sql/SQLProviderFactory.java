package org.xmodel.caching.sql;

import java.util.HashMap;
import java.util.Map;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.log.SLog;

/**
 * Factory for ISQLProvider instances.
 */
public class SQLProviderFactory
{
  public enum Provider { mysql};
  
  public SQLProviderFactory()
  {
    providers = new HashMap<String, ISQLProvider>();
  }
  
  /**
   * Returns an instance of ISQLProvider for the specified annotation.
   * @param annotation The caching policy annotation.
   * @return Returns an instance of ISQLProvider.
   */
  public synchronized static ISQLProvider getProvider( IModelObject annotation) throws ClassNotFoundException
  {
    String id = Xlate.childGet( annotation, "provider", (String)null);
    String host = Xlate.childGet( annotation, "host", (String)null);
    
    String key = String.format( "%s+%s", id, host);
    ISQLProvider provider = providers.get( key);
    if ( provider == null)
    {
      switch( Provider.valueOf( id))
      {
        case mysql:
          provider = new MySQLProvider();
          break;
          
        default:
          SLog.errorf( SQLProviderFactory.class, "Provider not found: %s", id);
          break;
      }
      
      if ( provider != null)
      {
        provider.configure( annotation);
        providers.put( key, provider);
      }
    }
    
    return provider;
  }
  
  private static Map<String, ISQLProvider> providers;
}
