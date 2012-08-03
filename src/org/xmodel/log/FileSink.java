package org.xmodel.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;

/**
 * An implementation of Log.ISink that logs to rolling log files.
 */
public final class FileSink implements ILogSink
{
  public FileSink()
  {
    this( null, "", 2, (long)1e6);
  }
  
  /**
   * Create a FileSink with the specified properties.
   * @param logFolder The path to the folder where the log files will be written.
   * @param filePrefix The prefix for log files.
   * @param count The maximum number of log files.
   * @param size The maximum size of a log file.
   */
  public FileSink( String logFolder, String filePrefix, int maxFileCount, long maxFileSize)
  {
    if ( logFolder == null)
    {
      logFolder = System.getProperty( "org.xmodel.log.FileSink.logFolder");
      if ( logFolder == null) logFolder = "logs";
    }
    
    this.logFolder = new File( logFolder).getAbsoluteFile();
    this.filePrefix = filePrefix;
    this.maxFileCount = maxFileCount;
    this.maxFileSize = maxFileSize;
    this.queue = new LinkedBlockingQueue<String>();
    this.files = new ArrayList<String>();
    this.started = new AtomicBoolean( false);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.log.ILogSink#configure(org.xmodel.IModelObject)
   */
  @Override
  public void configure( IModelObject config)
  {
    logFolder = Xlate.get( config, "logFolder", Xlate.childGet( config, "logFolder", logFolder));
    filePrefix = Xlate.get( config, "filePrefix", Xlate.childGet( config, "filePrefix", filePrefix));
    maxFileCount = Xlate.get( config, "maxFileCount", Xlate.childGet( config, "maxFileCount", maxFileCount));
    maxFileSize = Xlate.get( config, "maxFileSize", Xlate.childGet( config, "maxFileSize", maxFileSize));
  }

  /**
   * Start the logging thread.
   */
  public synchronized void start()
  {
    init();
    
    if ( thread != null) stop();
    
    thread = new Thread( consumerRunnable, "File-Logging");
    thread.setDaemon( true);
    thread.start();
  }
  
  /**
   * Stop the logging thread.
   */
  public synchronized void stop()
  {
    thread.interrupt();
    thread = null;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String)
   */
  @Override
  public void log( Log log, int level, Object message)
  {
    if ( !started.getAndSet( true)) start();
    queue.offer( (message != null)? message.toString(): "null");
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.Throwable)
   */
  @Override
  public void log( Log log, int level, Throwable throwable)
  {
    if ( !started.getAndSet( true)) start();
    queue.offer( throwable.toString());
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String, java.lang.Throwable)
   */
  @Override
  public void log( Log log, int level, Object message, Throwable throwable)
  {
    if ( !started.getAndSet( true)) start();
    queue.offer( ((message != null)? message: "null") + "\n" + throwable);
  }
  
  /**
   * Queue processing loop.
   */
  private void queueLoop()
  {
    List<String> messages = new ArrayList<String>();
    try
    {
      roll();
      
      while( true)
      {
        messages.clear();
        messages.add( queue.take());
        queue.drainTo( messages);

        for( String message: messages)
        {
          message = message + "\n";
          byte[] bytes = message.getBytes();
          stream.write( bytes);
          
          size += bytes.length;
          if ( size > maxFileSize) roll();
        }
        
        stream.flush();
      }
    }
    catch( InterruptedException e)
    {
      Thread.interrupted();
      try
      {
        stream.write( "\n** Logging Thread Interrupted **\n".getBytes());
        stream.flush();
      }
      catch( Exception e2)
      {
        System.err.println( e.getMessage());
      }
    }
    catch( Exception e)
    {
      try
      {
        stream.write( "\n** Logging Thread Caught Exception **\n".getBytes());
        stream.write( e.getMessage().getBytes());
        stream.flush();
      }
      catch( Exception e2)
      {
        System.err.println( e.getMessage());
      }
    }
  }
  
  /**
   * Create a new log file.
   */
  private void roll() throws IOException
  {
    if ( stream != null)
    {
      size = 0;
      stream.close();
      stream = null;
    }

    if ( !logFolder.exists()) logFolder.mkdirs();
    
    String name = String.format( "%s%s.log", filePrefix, dateFormat.format( new Date()));
    stream = new FileOutputStream( new File( logFolder, name));

    files.add( name);
    
    int extra = files.size() - maxFileCount;
    for( int i=0; i<extra; i++)
    {
      File file = new File( logFolder, files.get( 0));
      files.remove( 0);
      file.delete();
      file = null;
    }
  }
  
  /**
   * Initialize by loading the current log files and creating the first log.
   */
  private void init()
  {
    if ( logFolder.exists())
    {
      FilenameFilter filter = new FilenameFilter() {
        @Override public boolean accept( File folder, String name)
        {
          return name.startsWith( filePrefix) && fileRegex.matcher( name.substring( filePrefix.length())).matches();
        }
      };
      
      for( String name: logFolder.list( filter))
        files.add( name);
      
      Collections.sort( files, lastModifiedComparator);
    }
  }

  private Runnable consumerRunnable = new Runnable() {
    public void run()
    {
      queueLoop();
    }
  };
  
  private final static Comparator<String> lastModifiedComparator = new Comparator<String>() {
    public int compare( String path1, String path2)
    {
      File file1 = new File( path1);
      File file2 = new File( path2);
      return (int)(file1.lastModified() - file2.lastModified());
    }
  };

  private final static DateFormat dateFormat = new SimpleDateFormat( "MMddyy_HHmmss");
  private final static Pattern fileRegex = Pattern.compile( "^\\d{6}_\\d{6}\\.log$");

  private File logFolder;
  private String filePrefix;
  private int maxFileCount;
  private long maxFileSize;
  private long size;
  private List<String> files;
  private OutputStream stream;
  private BlockingQueue<String> queue;
  private Thread thread;
  private AtomicBoolean started;
}
