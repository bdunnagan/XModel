package org.xmodel.log;

import org.xmodel.IModelObject;

/**
 * An interface for handling a log message.
 */
public interface ILogSink
{
  /**
   * Configure this sink. 
   * @param config The configuration element.
   */
  public void configure( IModelObject config);
  
  /**
   * Log a message.
   * @param log The log.
   * @param level The log level.
   * @param message The message.
   */
  public void log( Log log, int level, Object message);
  
  /**
   * Log an exception.
   * @param log The log.
   * @param level The log level.
   * @param throwable The throwable.
   */
  public void log( Log log, int level, Throwable throwable);
  
  /**
   * Log a throwable with a message..
   * @param log The log.
   * @param level The log level.
   * @param message The message.
   * @param throwable The throwable.
   */
  public void log( Log log, int level, Object message, Throwable throwable);
}