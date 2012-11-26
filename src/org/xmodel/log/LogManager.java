package org.xmodel.log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
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
    period = 10000;
    
    String path = System.getProperty( "org.xmodel.log.config");
    if ( path == null) path = "logging.xml";
    
    config = new File( path);
    if ( !config.exists()) 
    {
      SLog.warnf( this, "Logging configuration file not found, %s", new File( path).getAbsolutePath());
      config = null;
    }

    Thread.setDefaultUncaughtExceptionHandler( new UncaughtExceptionLogger());
    
    if ( config != null) start();
  }
  
  /**
   * Start the configuration monitoring thread.
   */
  public void start()
  {
    if ( thread != null) stop(); else updateConfig();
    
    thread = new Thread( this, "Log-Config");
    thread.setDaemon( true);
    thread.setPriority( Thread.MIN_PRIORITY);
    thread.start();
  }
  
  /**
   * Stop the configuration monitoring thread.
   */
  public void stop()
  {
    if ( thread != null) 
    {
      thread.interrupt();
      thread = null;
    }
  }
  
  /**
   * Create an array of ILogSink instances from the specified configuration.
   * @param config The configuration element.
   * @return Returns the array of ILogSink instances.
   */
  protected static ILogSink[] configure( IModelObject config)
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
    
    return list.toArray( new ILogSink[ 0]);
  }
  
  /**
   * Update the logging configuration.
   */
  private void updateConfig()
  {
    try
    {
      IModelObject root = new XmlIO().read( new BufferedInputStream( new FileInputStream( config)));
      period = Xlate.childGet( root, "reload", 5) * 1000;

      ILogSink[] defaultSinks = configure( root);
      if ( defaultSinks.length == 1)
      {
        Log.setDefaultSink( defaultSinks[ 0]);
      }
      else if ( defaultSinks.length > 1)
      {
        Log.setDefaultSink( new MultiSink( defaultSinks));
      }
      
      for( IModelObject child: root.getChildren( "log"))
      {
        String name = Xlate.get( child, "name", (String)null);
        String level = Xlate.get( child, "level", (String)null);
        if ( name != null)
        {
          Log log = Log.getLog( name);
          
          if ( level != null)
            log.setLevel( Log.getLevelIndex( level));
          
          ILogSink[] sinks = configure( child);
          if ( sinks.length == 1)
          {
            log.setSink( sinks[ 0]);
          }
          else if ( sinks.length > 1)
          {
            log.setSink( new MultiSink( sinks));
          }
        }
      }
    }
    catch( Exception e)
    {
      SLog.warnf( this, "Unable to parse logging configuration - %s", e.getMessage());
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run()
  {
    while( true)
    {
      long modified = config.lastModified();
      if ( timestamp != 0 && modified > timestamp)
      {
        if ( timestamp > 0) SLog.warn( this, "Reloaded logging configuration.");
        timestamp = modified;
        updateConfig();
      }

      if ( period < 0) break;
      if ( period < 1000) period = 1000;
      try { Thread.sleep( period);} catch( Exception e) { break;}
    }
  }

  private File config;
  private long timestamp;
  private int period;
  private Thread thread;
}
