package org.xmodel.log;

/**
 * A class that provides static logging methods.
 */
public final class SLog
{
  /**
   * Returns true if the specified log level is enabled. 
   * @param client The object doing the logging.
   * @param level The log level.
   * @return True if level is enabled.
   */
  public static <T> boolean isLevelEnabled( T client, int level)
  {
    Log log = Log.getLog( client);
    return log.isLevelEnabled( level);
  }
    
  /**
   * Log a verbose message.
   * @param client The object doing the logging.
   * @param message The message.
   */
  public static <T> void verbose( T client, Object message)
  {
    Log log = Log.getLog( client);
    log.log( Log.verbose, message);
  }
  
  /**
   * Log a verbose message with a caught exception.
   * @param client The object doing the logging.
   * @param message The message.
   * @param Throwable The throwable that was caught.
   */
  public static <T> void verbose( T client, Object message, Throwable throwable)
  {
    Log log = Log.getLog( client);
    log.log( Log.verbose, message, throwable);
  }
  
  /**
   * Log a verbose message.
   * @param client The object doing the logging.
   * @param format The format.
   * @param params The format arguments.
   */
  public static <T> void verbosef( T client, String format, Object... params)
  {
    Log log = Log.getLog( client);
    log.logf( Log.verbose, format, params);
  }
  
  /**
   * Log a debug message.
   * @param client The object doing the logging.
   * @param message The message.
   */
  public static <T> void debug( T client, Object message)
  {
    Log log = Log.getLog( client);
    log.log( Log.debug, message);
  }
  
  /**
   * Log a debug message with a caught exception.
   * @param client The object doing the logging.
   * @param message The message.
   * @param Throwable The throwable that was caught.
   */
  public static <T> void debug( T client, Object message, Throwable throwable)
  {
    Log log = Log.getLog( client);
    log.log( Log.debug, message, throwable);
  }
  
  /**
   * Log a debug message.
   * @param client The object doing the logging.
   * @param format The format.
   * @param params The format arguments.
   */
  public static <T> void debugf( T client, String format, Object... params)
  {
    Log log = Log.getLog( client);
    log.logf( Log.debug, format, params);
  }
  
  /**
   * Log an information message.
   * @param client The object doing the logging.
   * @param message The message.
   */
  public static <T> void info( T client, Object message)
  {
    Log log = Log.getLog( client);
    log.log( Log.info, message);
  }
  
  /**
   * Log a info message with a caught exception.
   * @param client The object doing the logging.
   * @param message The message.
   * @param Throwable The throwable that was caught.
   */
  public static <T> void info( T client, Object message, Throwable throwable)
  {
    Log log = Log.getLog( client);
    log.log( Log.info, message, throwable);
  }
  
  /**
   * Log an information message.
   * @param client The object doing the logging.
   * @param format The format.
   * @param params The format arguments.
   */
  public static <T> void infof( T client, String format, Object... params)
  {
    Log log = Log.getLog( client);
    log.logf( Log.info, format, params);
  }
  
  /**
   * Log a warning message.
   * @param client The object doing the logging.
   * @param message The message.
   */
  public static <T> void warn( T client, Object message)
  {
    Log log = Log.getLog( client);
    log.log( Log.warn, message);
  }
  
  /**
   * Log a warning message with a caught exception.
   * @param client The object doing the logging.
   * @param message The message.
   * @param Throwable The throwable that was caught.
   */
  public static <T> void warn( T client, Object message, Throwable throwable)
  {
    Log log = Log.getLog( client);
    log.log( Log.warn, message, throwable);
  }
  
  /**
   * Log a warning message.
   * @param client The object doing the logging.
   * @param format The format.
   * @param params The format arguments.
   */
  public static <T> void warnf( T client, String format, Object... params)
  {
    Log log = Log.getLog( client);
    log.logf( Log.warn, format, params);
  }
  
  /**
   * Log an error message.
   * @param client The object doing the logging.
   * @param message The message.
   */
  public static <T> void error( T client, Object message)
  {
    Log log = Log.getLog( client);
    log.log( Log.error, message);
  }
  
  /**
   * Log an error message with a caught exception.
   * @param client The object doing the logging.
   * @param message The message.
   * @param Throwable The throwable that was caught.
   */
  public static <T> void error( T client, Object message, Throwable throwable)
  {
    Log log = Log.getLog( client);
    log.log( Log.error, message, throwable);
  }
  
  /**
   * Log an error message.
   * @param client The object doing the logging.
   * @param format The format.
   * @param params The format arguments.
   */
  public static <T> void errorf( T client, String format, Object... params)
  {
    Log log = Log.getLog( client);
    log.logf( Log.error, format, params);
  }
  
  /**
   * Log a severe message.
   * @param client The object doing the logging.
   * @param message The message.
   */
  public static <T> void severe( T client, Object message)
  {
    Log log = Log.getLog( client);
    log.log( Log.severe, message);
  }
  
  /**
   * Log a severe message with a caught exception.
   * @param client The object doing the logging.
   * @param message The message.
   * @param Throwable The throwable that was caught.
   */
  public static <T> void severe( T client, Object message, Throwable throwable)
  {
    Log log = Log.getLog( client);
    log.log( Log.severe, message, throwable);
  }
  
  /**
   * Log a severe message.
   * @param client The object doing the logging.
   * @param format The format.
   * @param params The format arguments.
   */
  public static <T> void severef( T client, String format, Object... params)
  {
    Log log = Log.getLog( client);
    log.logf( Log.severe, format, params);
  }
  
  /**
   * Log a fatal message.
   * @param client The object doing the logging.
   * @param message The message.
   */
  public static <T> void fatal( T client, Object message)
  {
    Log log = Log.getLog( client);
    log.log( Log.fatal, message);
  }
  
  /**
   * Log a fatal message with a caught exception.
   * @param client The object doing the logging.
   * @param message The message.
   * @param Throwable The throwable that was caught.
   */
  public static <T> void fatal( T client, Object message, Throwable throwable)
  {
    Log log = Log.getLog( client);
    log.log( Log.fatal, message, throwable);
  }
  
  /**
   * Log a fatal message.
   * @param client The object doing the logging.
   * @param format The format.
   * @param params The format arguments.
   */
  public static <T> void fatalf( T client, String format, Object... params)
  {
    Log log = Log.getLog( client);
    log.logf( Log.fatal, format, params);
  }
  
  /**
   * Log an exception.
   * @param client The object doing the logging.
   * @param throwable The exception.
   */
  public static <T> void exception( T client, Throwable throwable)
  {
    Log log = Log.getLog( client);
    log.exception( throwable);
  }
  
  /**
   * Log an exception with a message.
   * @param client The object doing the logging.
   * @param throwable The exception.
   * @param format The format.
   * @param params The format arguments.
   */
  public static <T> void exceptionf( T client, Throwable throwable, String format, Object... params)
  {
    Log log = Log.getLog( client.getClass());
    log.exceptionf( throwable, format, params);
  }
}
