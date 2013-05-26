package org.xmodel.log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogMap
{
  protected LogMap()
  {
    this.map = new ConcurrentHashMap<String, Log>();
    this.configs = new ConcurrentHashMap<String, Config>();
  }
  
  /**
   * @return Returns the singleton.
   */
  public static LogMap getInstance()
  {
    return instance;
  }
  
  /**
   * Get and/or create the log with the specified name.
   * @param name The name of the log.
   * @return Returns the Log instance for the specified name.
   */
  public Log getLog( String name)
  {
    Log log = map.get( name);
    return (log != null)? log: newLog( name);
  }
  
  /**
   * Configure all logs, including logs created in the future, that match the specified pattern.
   * @param pattern The regular expression.
   * @param level The level.
   */
  public void configure( Pattern pattern, int level)
  {
    Config config = configs.get( pattern.pattern());
    if ( config == null)
    {
      synchronized( this)
      {
        // save to configure future logs
        config = new Config( pattern, level);
        configs.put( pattern.pattern(), config);
      }
    }
    
    // configure current logs
    for( Log log: findLogs( pattern))
    {
      log.setLevel( level);
    }
  }
  
  /**
   * Find all logs matching the specified pattern.
   * @param pattern The pattern.
   * @return Returns the matching logs.
   */
  public List<Log> findLogs( Pattern pattern)
  {
    List<Log> logs = new ArrayList<Log>();
    Matcher matcher = pattern.matcher( "");
    Iterator<Entry<String, Log>> iterator = map.entrySet().iterator();
    while( iterator.hasNext())
    {
      Entry<String, Log> entry = iterator.next();
      matcher.reset( entry.getKey());
      if ( matcher.matches()) logs.add( entry.getValue());
    }
    return logs;
  }
  
  /**
   * Create new log.
   * @param name The name of the log.
   * @return Returns the new log.
   */
  private synchronized Log newLog( String name)
  {
    Log log = new Log();
    map.put( name, log);
    
    for( Entry<String, Config> entry: configs.entrySet())
    {
      Config config = entry.getValue();
      config.matcher.reset( name);
      if ( config.matcher.matches())
      {
        log.setLevel( config.level);
      }
    }
    
    return log;
  }
  
  private static class Config
  {
    public Config( Pattern pattern, int level)
    {
      this.matcher = pattern.matcher( "");
      this.level = level;
    }
    
    public Matcher matcher;
    public int level;
  }

  private final static LogMap instance = new LogMap();
  
  private ConcurrentHashMap<String, Log> map;
  private ConcurrentHashMap<String, Config> configs;
}
