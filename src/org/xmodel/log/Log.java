package org.xmodel.log;

import org.apache.log4j.Logger;

/**
 * Convenience class for automatically setting the default logging format.
 */
public class Log
{
  /**
   * Returns the logger with the specified name.
   * @param name The name of the logger.
   * @return Returns the logger with the specified name.
   */
  public static Logger getLog( String name)
  {
    return Logger.getLogger( name);
  }
}
