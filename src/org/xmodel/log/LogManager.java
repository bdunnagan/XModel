package org.xmodel.log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.log.mbean.Logging;
import org.xmodel.xml.XmlIO;

/**
 * An instance of this class will monitor a file in the file-system periodically for changes
 * to the logging facility configuration.  The location of the file may be specified in the
 * constructor or in the system property, "org.xmodel.log.config".  The period may be
 * specified in the configuration file element, "reload".
 */
public final class LogManager implements Runnable
{
  public static class UncaughtExceptionLogger implements UncaughtExceptionHandler
  {
    /* (non-Javadoc)
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException( Thread thread, Throwable thrown)
    {
      Log log = Log.getLog( LogManager.class);
      log.severe( thrown.getMessage());
      log.exception( thrown);
    }
  }
  
  /**
   * Create and start monitoring if the system property is set.
   */
  public LogManager()
  {
    period = 10;
    log = Log.getLog( this);

    // get logging configuration
    String path = System.getProperty( "org.xmodel.log.config");
    if ( path == null) path = "logging.xml";
    
    log.infof( "LogManager created with configuration: %s", path);
    
    config = new File( path);
    if ( !config.exists()) 
    {
      log.warnf( "Logging configuration file not found, %s", new File( path).getAbsolutePath());
      config = null;
    }

    // log uncaught exceptions
    Thread.setDefaultUncaughtExceptionHandler( new UncaughtExceptionLogger());
    
    // initialize logging mbean
    Logging.getInstance();
    
    // start supervisor schedule
    if ( config != null)
    {
      scheduler = Executors.newScheduledThreadPool( 1, new ThreadFactory() {
        @Override public Thread newThread( Runnable runnable)
        {
          Thread thread = new Thread( runnable, "Logvisor");
          thread.setDaemon( true);
          return thread;
        }
      });
      
      run();
    }
  }
  
  /**
   * Create an ILogSink instance from the specified configuration.
   * @param config The configuration element.
   * @return Returns the ILogSink instance.
   */
  protected static ILogSink configure( IModelObject config)
  {
    List<ILogSink> list = new ArrayList<ILogSink>( 3);
    for( IModelObject child: config.getChildren( "sink"))
    {
      String cname = Xlate.get( child, "class", (String)null);
      if ( cname != null)
      {
        try
        {
          Class<?> clazz = LogManager.class.getClassLoader().loadClass( cname);
          ILogSink sink = (ILogSink)clazz.newInstance();
          sink.configure( child);
          list.add( sink);
        }
        catch( Exception e)
        {
          SLog.exception( LogManager.class, e);
        }
      }
    }
    
    if ( list.size() == 0) return null;
    if ( list.size() == 1) return list.get( 0);
    return new MultiSink( list.toArray( new ILogSink[ 0]));
  }
  
  /**
   * Update the logging configuration.
   */
  private void updateConfig()
  {
    try
    {
      IModelObject root = new XmlIO().read( new BufferedInputStream( new FileInputStream( config)));
      period = Xlate.childGet( root, "reload", period);

      ILogSink defaultSink = configure( root);
      Log.setDefaultSink( defaultSink);
      
      for( IModelObject child: root.getChildren( "log"))
      {
        String level = Xlate.get( child, "level", (String)null);
        
        ILogSink sink = configure( child);
        if ( sink == null) sink = defaultSink;
        
        String name = Xlate.get( child, "name", (String)null);
        if ( name != null)
        {
          Log log = Log.getLog( name);
          if ( sink != null) log.setSink( sink);
          if ( level != null) log.setLevel( Log.getLevelIndex( level));
        }
        
        String regex = Xlate.get( child, "regex", (String)null);
        if ( regex != null)
        {
          try
          {
            for( Log log: LogMap.getInstance().findLogs( Pattern.compile( regex)))
            {
              if ( sink != null) log.setSink( sink);
              if ( level != null) log.setLevel( Log.getLevelIndex( level));
            }
          }
          catch( Exception e)
          {
            log.warnf( "Unable to regex, %s", e.toString());
          }
        }
      }
    }
    catch( Exception e)
    {
      log.warnf( "Unable to parse logging configuration, %s", e.toString());
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run()
  {
    long modified = config.lastModified();
    if ( timestamp == 0 || modified > timestamp)
    {
      log.info( "Logging configuration updated.");
      timestamp = modified;
      updateConfig();
    }
    
    if ( period <= 0) period = 1;
    
    if ( future == null || future.cancel( false))
      future = scheduler.schedule( this, period, TimeUnit.SECONDS);
  }

  private Log log;
  private File config;
  private long timestamp;
  private int period;
  private ScheduledExecutorService scheduler;
  private ScheduledFuture<?> future;
}
