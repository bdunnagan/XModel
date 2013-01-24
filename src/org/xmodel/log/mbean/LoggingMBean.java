package org.xmodel.log.mbean;

public interface LoggingMBean
{
  /**
   * Returns the logging level for the specified log.
   * @param log The name of the log.
   * @return Returns the logging level.
   */
  public String getLevel( String log) throws Exception;
  
  /**
   * Set the logging level for the specified log.
   * @param log The name of the log.
   * @param level The name of the new logging level.
   */
  public void setLevel( String log, String level) throws Exception;
}
