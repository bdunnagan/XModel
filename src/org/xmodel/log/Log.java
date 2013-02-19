package org.xmodel.log;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

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
  public final static int problems = exception | info | warn | error | severe | fatal;
  public final static int all = 0xff;

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
      default:            break;
    }
    
    if ( level == 0) return "none";
    if ( (level ^ all) == 0) return "all";
    if ( (level ^ problems) == 0) return "problems";
    
    StringBuilder sb = new StringBuilder();
    if ( (level & exception) != 0) sb.append( "exception, ");
    if ( (level & debug) != 0) sb.append( "debug, ");
    if ( (level & info) != 0) sb.append( "info, ");
    if ( (level & warn) != 0) sb.append( "warn, ");
    if ( (level & error) != 0) sb.append( "error, ");
    if ( (level & severe) != 0) sb.append( "severe, ");
    if ( (level & fatal) != 0) sb.append( "fatal, ");
    if ( sb.length() > 0) sb.setLength( sb.length() - 2);
    return sb.toString();
  }

  /**
   * Returns the logging level given the case-insensitive name of the level.
   * @param name The name of the logging level.
   * @return Returns the logging level given the case-insensitive name of the level.
   */
  public final static int getLevelIndex( String name)
  {
    int level = 0;
    int i = 0;
    while( i < name.length())
    {
      int j = name.indexOf( ',', i);
      if ( j < 0) j = name.length();

      String part = name.substring( i, j).trim();
      if ( part.equalsIgnoreCase( "verbose")) level |= Log.verbose;
      if ( part.equalsIgnoreCase( "debug")) level |= Log.debug;
      if ( part.equalsIgnoreCase( "info")) level |= Log.info;
      if ( part.equalsIgnoreCase( "warn")) level |= Log.warn;
      if ( part.equalsIgnoreCase( "error")) level |= Log.error;
      if ( part.equalsIgnoreCase( "severe")) level |= Log.severe;
      if ( part.equalsIgnoreCase( "fatal")) level |= Log.fatal;
      if ( part.equalsIgnoreCase( "exception")) level |= Log.exception;
      if ( part.equalsIgnoreCase( "problems")) level |= Log.problems;
      if ( part.equalsIgnoreCase( "all")) level |= Log.all;
      
      i = j+1;
    }
    
    return level;
  }
    
  /**
   * Returns the log with the specified class name.
   * @param name The name of the log.
   * @return Returns the log with the specified name.
   */
  public static Log getLog( String name)
  {
    return map.getLog( name);
  }
  
  /**
   * Returns the log with the name of the package in which the specified class is declared..
   * @param clazz The class.
   * @return The log.
   */
  public static Log getLog( Class<?> clazz)
  {
    return getLog( clazz.getName());
  }
  
  /**
   * Returns a unique Log for the package in which the specified object is defined.
   * @param object The object.
   * @return The unique Log for the object.
   */
  public static Log getLog( Object object)
  {
    if ( object instanceof String)
    {
      throw new IllegalArgumentException( String.format(
          "SLog invocation missing object argument in class: %s", 
          object.getClass().getName()));
    }
  
    return getLog( object.getClass().getName());
  }
  
  /**
   * Set the logging level for all logs whose names match the specified regular expression.
   * @param regex The regular expression.
   * @param level The new logging level.
   */
  public static void setLevel( String regex, int level)
  {
    for( Log log: map.findLogs( Pattern.compile( regex)))
      log.setLevel( level);
  }
  
  /**
   * Set the default implementation of ILogSink used by newly created Log instances.
   * @param sink The sink.
   */
  public static void setDefaultSink( ILogSink sink)
  {
    defaultSink.set( sink);
  }
  
  protected Log()
  {
    this.mask = new AtomicInteger( problems | info);
    this.sink = null;
  }
  
  /**
   * Set the log level.
   * @param level The level.
   */
  public void setLevel( int level)
  {
    mask.set( level);
  }
  
  /**
   * @return Returns the log level.
   */
  public int getLevel()
  {
    return mask.get();
  }
    
  /**
   * Returns true if the specified logging level is enabled.
   * @param level The logging level.
   * @return Returns true if the specified logging level is enabled.
   */
  public boolean isLevelEnabled( int level)
  {
    return (mask.get() & level) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean verbose()
  {
    return (mask.get() & Log.verbose) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean debug()
  {
    return (mask.get() & Log.debug) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean info()
  {
    return (mask.get() & Log.info) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean warn()
  {
    return (mask.get() & Log.warn) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean error()
  {
    return (mask.get() & Log.error) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean severe()
  {
    return (mask.get() & Log.severe) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean fatal()
  {
    return (mask.get() & Log.fatal) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean exception()
  {
    return (mask.get() & Log.exception) != 0;
  }
  
  /**
   * Log a verbose message.
   * @param message The message.
   */
  public void verbose( Object message)
  {
    log( verbose, message);
  }
  
  /**
   * Log a verbose message with a caught exception.
   * @param message The message.
   * @param Throwable The throwable that was caught.
   */
  public void verbose( Object message, Throwable throwable)
  {
    log( verbose, message, throwable);
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
  public void debug( Object message)
  {
    log( debug, message);
  }
  
  /**
   * Log a debug message with a caught exception.
   * @param message The message.
   * @param Throwable The throwable that was caught.
   */
  public void debug( Object message, Throwable throwable)
  {
    log( debug, message, throwable);
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
  public void info( Object message)
  {
    log( info, message);
  }
  
  /**
   * Log a info message with a caught exception.
   * @param message The message.
   * @param Throwable The throwable that was caught.
   */
  public void info( Object message, Throwable throwable)
  {
    log( info, message, throwable);
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
  public void warn( Object message)
  {
    log( warn, message);
  }
  
  /**
   * Log a warning message with a caught exception.
   * @param message The message.
   * @param Throwable The throwable that was caught.
   */
  public void warn( Object message, Throwable throwable)
  {
    log( warn, message, throwable);
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
  public void error( Object message)
  {
    log( error, message);
  }
  
  /**
   * Log an error message with a caught exception.
   * @param message The message.
   * @param Throwable The throwable that was caught.
   */
  public void error( Object message, Throwable throwable)
  {
    log( error, message, throwable);
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
  public void severe( Object message)
  {
    log( severe, message);
  }
  
  /**
   * Log a severe message with a caught exception.
   * @param message The message.
   * @param Throwable The throwable that was caught.
   */
  public void severe( Object message, Throwable throwable)
  {
    log( severe, message, throwable);
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
  public void fatal( Object message)
  {
    log( fatal, message);
  }
  
  /**
   * Log a fatal message with a caught exception.
   * @param message The message.
   * @param Throwable The throwable that was caught.
   */
  public void fatal( Object message, Throwable throwable)
  {
    log( fatal, message, throwable);
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
   * Log an exception.
   * @param throwable The exception.
   */
  public void exception( Throwable throwable)
  {
    if ( (mask.get() & exception) == 0) return;
    
    if ( sink == null) sink = defaultSink.get();
    sink.log( this, exception, throwable);
    
    Throwable cause = throwable.getCause();
    if ( cause != null && cause != throwable)
      exceptionf( cause, "Caused by: ");
  }
  
  /**
   * Log an exception with a message.
   * @param throwable The exception.
   * @param format The format.
   * @param params The format arguments.
   */
  public void exceptionf( Throwable throwable, String format, Object... params)
  {
    if ( (mask.get() & exception) == 0) return;
    
    if ( sink == null) sink = defaultSink.get();
    sink.log( this, exception, String.format( format, params), throwable);
    
    Throwable cause = throwable.getCause();
    if ( cause != null && cause != throwable)
      exceptionf( cause, "Caused by: ");
  }

  /**
   * Log a message.
   * @param level The logging level.
   * @param message The message.
   */
  public void log( int level, Object message)
  {
    if ( (mask.get() & level) == 0) return;
    try
    {
      if ( sink == null) sink = defaultSink.get();
      sink.log( this, level, message);
    }
    catch( Exception e)
    {
      log( error, e.toString());
    }
  }
  
  /**
   * Log a message with a caught exception.
   * @param level The logging level.
   * @param message The message.
   * @param throwable The throwable that was caught.
   */
  public void log( int level, Object message, Throwable throwable)
  {
    if ( (mask.get() & level) == 0) return;
    try
    {
      if ( sink == null) sink = defaultSink.get();
      sink.log( this, level, message, throwable);
    }
    catch( Exception e)
    {
      log( error, e.toString());
    }
  }
  
  /**
   * Log a message.
   * @param level The logging level.
   * @param format The format.
   * @param params The format arguments.
   */
  public void logf( int level, String format, Object... params)
  {
    if ( (mask.get() & level) == 0) return;
    try
    {
      if ( sink == null) sink = defaultSink.get();
      sink.log( this, level, String.format( format, params));
    }
    catch( Exception e)
    {
      log( error, e.toString());
    }
  }
  
  protected static LogMap map = LogMap.getInstance();
  private static AtomicReference<ILogSink> defaultSink = new AtomicReference<ILogSink>( new FormatSink( new ConsoleSink()));
    
  private AtomicInteger mask;
  private ILogSink sink;
}
