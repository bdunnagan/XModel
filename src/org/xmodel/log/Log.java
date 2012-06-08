package org.xmodel.log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
    }
    return null;
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
      int j = name.indexOf( ',');
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
   * Returns the log with the specified name.
   * @param name The name of the log.
   * @return Returns the log with the specified name.
   */
  public synchronized static Log getLog( String name)
  {
    Log log = logs.get( name);
    if ( log == null) log = new Log();
    logs.put( name, log);
    return log;
  }
  
  /**
   * Returns the log with the name of the package in which the specified class is declared..
   * @param clazz The class.
   * @return The log.
   */
  public static Log getLog( Class<?> clazz)
  {
    String name = clazz.getName();
    int index = name.lastIndexOf( '.');
    if ( index < 0) return getLog( name);
    return getLog( name.substring( 0, index));
  }
  
  /**
   * Returns a unique Log for the package in which the specified object is defined.
   * @param object The object.
   * @return The unique Log for the object.
   */
  public static Log getLog( Object object)
  {
    if ( object instanceof String)
      throw new IllegalArgumentException(
          "Illegal to log on behalf of String class.");
    
    Map<Object, Log> map = threadLogs.get();
    if ( map == null)
    {
      map = new HashMap<Object, Log>();
      threadLogs.set( map);
    }
    
    Log log = map.get( object);
    if ( log == null)
    {
      log = getLog( object.getClass());
      map.put( object, log);
    }
    
    return log;
  }
  
  private Log()
  {
    this.mask = new AtomicInteger( problems | info);
    this.sink = null;
    
    if ( fileSink == null)
    {
      consoleSink = new ConsoleSink();
      try { fileSink = new FileSink( "logs", "", 2, (long)1e6);} catch( Exception e) {}
      //try { syslogSink = new SyslogSink();} catch( Exception e) {}
    }
    
    //sink = new FormatSink( new MultiSink( fileSink, consoleSink, syslogSink));
    sink = new FormatSink( new MultiSink( fileSink, consoleSink));
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
   * Set the log sink (not thread-safe).
   * @param sink The log sink.
   */
  public synchronized void setSink( ILogSink sink)
  {
    this.sink = sink;
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
    
    ILogSink sink;
    synchronized( this)
    {
      sink = this.sink;
    }
    
    if ( sink != null) sink.log( this, exception, throwable);
    
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
    
    ILogSink sink;
    synchronized( this)
    {
      sink = this.sink;
    }
    
    if ( sink != null) sink.log( this, exception, String.format( format, params), throwable);
    
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
    
    ILogSink sink;
    synchronized( this)
    {
      sink = this.sink;
    }
    
    try
    {
      if ( sink != null) sink.log( this, level, message);
    }
    catch( Exception e)
    {
      log( error, String.format( "Caught exception in log event for message, '%s'", message), e);
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
    
    ILogSink sink;
    synchronized( this)
    {
      sink = this.sink;
    }
    
    try
    {
      if ( sink != null) sink.log( this, level, message, throwable);
    }
    catch( Exception e)
    {
      log( error, e.getMessage());
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
    
    ILogSink sink;
    synchronized( this)
    {
      sink = this.sink;
    }
    
    try
    {
      if ( sink != null) sink.log( this, level, String.format( format, params));
    }
    catch( Exception e)
    {
      log( error, String.format( "Caught exception in log event for message with format, '%s'", format), e);
    }
  }

  /**
   * Map of logs by name.
   */
  private static Map<String, Log> logs = Collections.synchronizedMap( new HashMap<String, Log>());
  
  /**
   * Thread-local map of logs by logging source object.
   */
  private static ThreadLocal<Map<Object, Log>> threadLogs = new ThreadLocal<Map<Object, Log>>();

  private static FileSink fileSink;
  private static ConsoleSink consoleSink;
  private static SyslogSink syslogSink;
  
  private AtomicInteger mask;
  private ILogSink sink;
}
