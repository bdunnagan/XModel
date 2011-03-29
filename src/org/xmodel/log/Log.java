package org.xmodel.log;

import java.util.HashMap;
import java.util.Map;

/**
 * Yet another logging facility.
 */
public final class Log
{
  public final static int exception = 0x80;
  public final static int verbose = 0x40;
  public final static int debug = 0x20;
  public final static int info = 0x10;
  public final static int warn = 0x08;
  public final static int error = 0x04;
  public final static int severe = 0x02;
  public final static int fatal = 0x01;
  public final static int problems = exception | warn | error | severe | fatal;
  
  /**
   * Returns the name of the specified logging level.
   * @param level The logging level.
   * @return Returns the name of the specified logging level.
   */
  public final static String getLevelName( int level)
  {
    switch( level)
    {
      case Log.exception: return "Exception";
      case Log.verbose:   return "Verbose";
      case Log.debug:     return "Debug";
      case Log.info:      return "Info";
      case Log.warn:      return "Warn";
      case Log.error:     return "Error";
      case Log.severe:    return "Severe";
      case Log.fatal:     return "Fatal";
    }
    return null;
  }
  
  /**
   * An interface for handling a log message.
   */
  public interface ISink
  {
    /**
     * Log a message.
     * @param level The log level.
     * @param message The message.
     */
    public void log( int level, String message);
    
    /**
     * Log an exception.
     * @param level The log level.
     * @param throwable The throwable.
     */
    public void log( int level, Throwable throwable);
    
    /**
     * Log a throwable with a message..
     * @param level The log level.
     * @param message The message.
     * @param throwable The throwable.
     */
    public void log( int level, String message, Throwable throwable);
  }
  
  /**
   * Returns the log with the specified name.
   * @param name The name of the log.
   * @return Returns the log with the specified name.
   */
  public static Log getLog( String name)
  {
    Log log = logs.get( name);
    if ( log == null) log = new Log();
    logs.put( name, log);
    return log;
  }
  
  public Log()
  {
    this( new ConsoleSink());
  }
  
  public Log( ISink sink)
  {
    this.mask = problems;
    this.sink = sink;
  }
  
  /**
   * Set the log level.
   * @param level The level.
   */
  public void setLevel( int level)
  {
    this.mask = level;
  }
  
  /**
   * Log an exception.
   * @param throwable The exception.
   */
  public void exception( Throwable throwable)
  {
    if ( (mask & exception) == 0) return;
    sink.log( exception, throwable);
  }
  
  /**
   * Log an exception with a message.
   * @param throwable The exception.
   * @param format The format.
   * @param params The format arguments.
   */
  public void exceptionf( Throwable throwable, String format, Object... params)
  {
    if ( (mask & exception) == 0) return;
    sink.log( exception, String.format( format, params), throwable);
  }

  /**
   * Log a verbose message.
   * @param message The message.
   */
  public void verbose( String message)
  {
    log( verbose, message);
  }
  
  /**
   * Log a verbose message.
   * @param format The format.
   * @param params The format arguments.
   */
  public void verbosef( String format, Object... params)
  {
    logf( verbose, format, params);
  }
  
  /**
   * Log a debug message.
   * @param message The message.
   */
  public void debug( String message)
  {
    log( debug, message);
  }
  
  /**
   * Log a debug message.
   * @param format The format.
   * @param params The format arguments.
   */
  public void debugf( String format, Object... params)
  {
    logf( debug, format, params);
  }
  
  /**
   * Log an information message.
   * @param message The message.
   */
  public void info( String message)
  {
    log( info, message);
  }
  
  /**
   * Log an information message.
   * @param format The format.
   * @param params The format arguments.
   */
  public void infof( String format, Object... params)
  {
    logf( info, format, params);
  }
  
  /**
   * Log a warning message.
   * @param message The message.
   */
  public void warn( String message)
  {
    log( warn, message);
  }
  
  /**
   * Log a warning message.
   * @param format The format.
   * @param params The format arguments.
   */
  public void warnf( String format, Object... params)
  {
    logf( warn, format, params);
  }
  
  /**
   * Log an error message.
   * @param message The message.
   */
  public void error( String message)
  {
    log( error, message);
  }
  
  /**
   * Log an error message.
   * @param format The format.
   * @param params The format arguments.
   */
  public void errorf( String format, Object... params)
  {
    logf( error, format, params);
  }
  
  /**
   * Log a severe message.
   * @param message The message.
   */
  public void severe( String message)
  {
    log( severe, message);
  }
  
  /**
   * Log a severe message.
   * @param format The format.
   * @param params The format arguments.
   */
  public void severef( String format, Object... params)
  {
    logf( severe, format, params);
  }
  
  /**
   * Log a fatal message.
   * @param message The message.
   */
  public void fatal( String message)
  {
    log( fatal, message);
  }
  
  /**
   * Log a fatal message.
   * @param format The format.
   * @param params The format arguments.
   */
  public void fatalf( String format, Object... params)
  {
    logf( fatal, format, params);
  }
  
  /**
   * Log a message.
   * @param level The logging level.
   * @param message The message.
   */
  private void log( int level, String message)
  {
    if ( (mask & level) == 0) return;
    sink.log( level, message);
  }
  
  /**
   * Log a message.
   * @param level The logging level.
   * @param format The format.
   * @param params The format arguments.
   */
  private void logf( int level, String format, Object... params)
  {
    if ( (mask & level) == 0) return;
    sink.log( level, String.format( format, params));
  }
  
  private static Map<String, Log> logs = new HashMap<String, Log>();
  
  private int mask;
  private ISink sink;
}
