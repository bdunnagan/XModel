package org.xmodel.log;

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
  public synchronized static Log getLog( String name)
  {
    return map.getCreateOne( name);
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
          "SLog invocation missing object argument in class: %", 
          object.getClass().getName()));
    }
  
    return getLog( object.getClass().getName());
  }
  
  /**
   * Set the logging level for all logs whose names begin with the specified prefix. Calling
   * this operation may globally impact performance of users of this logging framework.
   * @param query The log query.
   * @param level The new logging level.
   */
  public synchronized static void setLevel( String query, int level)
  {
    // make sure log exists
    map.getCreateOne( query);
    
    // iterate over matching logs
    for( Log log: map.getAll( query))
      log.setLevel( level);
  }
  
  /**
   * Set the sink to be used by all logs whose names begin with the specified prefix. Calling
   * this operation may globally impact performance of users of this logging framework.
   * @param prefix The prefix.
   * @param sink The sink.
   */
  public synchronized static void setSink( String prefix, ILogSink sink)
  {
    for( Log log: map.getAll( prefix))
      log.setSink( sink);
  }
  
  protected Log()
  {
    this.mask = problems | info;
    this.sink = defaultSink;
  }
  
  protected Log( Log log)
  {
    this.mask = log.mask;
    this.sink = log.sink;
  }
  
  /**
   * Set the log level.
   * @param level The level.
   */
  public void setLevel( int level)
  {
    mask = level;
  }
  
  /**
   * Set the log sink.
   * @param sink The log sink.
   */
  public void setSink( ILogSink sink)
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
    return (mask & level) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean verbose()
  {
    return (mask & Log.verbose) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean debug()
  {
    return (mask & Log.debug) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean info()
  {
    return (mask & Log.info) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean warn()
  {
    return (mask & Log.warn) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean error()
  {
    return (mask & Log.error) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean severe()
  {
    return (mask & Log.severe) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean fatal()
  {
    return (mask & Log.fatal) != 0;
  }
  
  /**
   * @return Returns true if the specified level is enabled.
   */
  public boolean exception()
  {
    return (mask & Log.exception) != 0;
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
    if ( (mask & exception) == 0) return;
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
    if ( (mask & exception) == 0) return;
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
    if ( (mask & level) == 0) return;
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
    if ( (mask & level) == 0) return;
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
    if ( (mask & level) == 0) return;
    try
    {
      if ( sink != null) sink.log( this, level, String.format( format, params));
    }
    catch( Exception e)
    {
      log( error, String.format( "Caught exception in log event for message with format, '%s'", format), e);
    }
  }
  
  protected static PackageMap map = new PackageMap();
  private static ILogSink defaultSink = new FormatSink( new MultiSink( new ConsoleSink(), new FileSink()));
  
  @SuppressWarnings("unused")
  private static LogManager configMonitor = new LogManager();
  
  private volatile int mask;
  private volatile ILogSink sink;
}
