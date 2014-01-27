package org.xmodel.log.mbean;

import java.lang.management.ManagementFactory;
import java.util.regex.Pattern;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.xmodel.log.Log;
import org.xmodel.log.LogMap;
import org.xmodel.log.SLog;

public class Logging implements LoggingMBean
{
  protected Logging()
  {
    try
    {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName name = new ObjectName( "org.xmodel.log:type=Logging");   
      mbs.registerMBean( this, name);
    }
    catch( Exception e)
    {
      SLog.warnf( this, "Failed to register logging mbean: %s", e.toString());
    }
  }

  /**
   * @return Returns the singleton.
   */
  public static Logging getInstance()
  {
    synchronized( Logging.class)
    {
      if ( instance == null) instance = new Logging();
    }
    return instance;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.log.mbean.LoggingMBean#getLevel(java.lang.String)
   */
  @Override
  public String getLevel( String log)
  {
    return Log.getLevelName( Log.getLog( log).getLevel());
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.mbean.LoggingMBean#setLevel(java.lang.String, java.lang.String)
   */
  @Override
  public void setLevel( String query, String level) throws Exception
  {
    int mask = Log.getLevelIndex( level);
    for( Log log: LogMap.getInstance().findLogs( Pattern.compile( query)))
    {
      log.setLevel( mask);
    }
  }
  
  private static Logging instance = null;
}
