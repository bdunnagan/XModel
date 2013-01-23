package org.xmodel.log.mbean;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.xmodel.log.Log;
import org.xmodel.log.SLog;

public class Logging implements LoggingMBean
{
  protected Logging()
  {
    try
    {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName name = new ObjectName( "org.xmodel.log.mbean:type=Logging");   
      mbs.registerMBean( this, name);
    }
    catch( Exception e)
    {
      SLog.exception( this, e);
    }
  }

  /**
   * @return Returns the singleton.
   */
  public static Logging getInstance()
  {
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
  public void setLevel( String log, String level)
  {
    Log.getLog( log).setLevel( Log.getLevelIndex( level));
  }
  
  private final static Logging instance = new Logging();
}
