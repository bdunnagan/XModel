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
import java.util.regex.Pattern;

/**
 * An implementation of Log.ISink that logs to rolling log files.
 */
public final class FileSink implements ILogSink
{
  /**
   * Create a FileSink with the specified properties.
   * @param path The path to the folder where the log files will be written.
   * @param prefix The prefix for log files.
   * @param count The maximum number of log files.
   * @param size The maximum size of a log file.
   */
  public FileSink( String path, String prefix, int maxCount, long maxSize) throws IOException
  {
    this.path = path;
    this.prefix = (prefix != null)? prefix: "";
    this.maxCount = maxCount;
    this.maxSize = maxSize;
    this.queue = new LinkedBlockingQueue<String>();
    this.files = new ArrayList<String>();
    
    init();
    start();
  }
  
  public synchronized void start()
  {
    if ( thread != null) stop();
    
    thread = new Thread( consumerRunnable, "Logging");
    thread.setDaemon( true);
    thread.start();
  }
  
  /**
   * Stop the file sync thread.
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
    queue.offer( (message != null)? message.toString(): "null");
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.Throwable)
   */
  @Override
  public void log( Log log, int level, Throwable throwable)
  {
    queue.offer( throwable.toString());
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String, java.lang.Throwable)
   */
  @Override
  public void log( Log log, int level, Object message, Throwable throwable)
  {
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
          if ( size > maxSize) roll();
        }
        
        stream.flush();
      }
    }
    catch( Exception e)
    {
      Thread.interrupted();
      System.err.println( e.getMessage());
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

    File folder = new File( path);
    if ( !folder.exists()) folder.mkdirs();
    
    String name = String.format( "%s%s.log", prefix, dateFormat.format( new Date()));
    stream = new FileOutputStream( new File( folder, name));

    files.add( name);
    
    while( files.size() > maxCount)
    {
      System.gc();
      
      int extra = files.size() - maxCount;
      for( int i=0; i<extra; i++)
      {
        File file = new File( path, files.get( 0));
        files.remove( 0);
        if ( file.delete())
        {
          file = null;
        }
        else
        {
          files.add( 0, file.getAbsolutePath());
        }
      }
    }
  }
  
  /**
   * Initialize by loading the current log files and creating the first log.
   */
  private void init() throws IOException
  {
    File folder = new File( path);
    if ( folder.exists())
    {
      FilenameFilter filter = new FilenameFilter() {
        @Override public boolean accept( File folder, String name)
        {
          return name.startsWith( prefix) && fileRegex.matcher( name.substring( prefix.length())).matches();
        }
      };
      
      for( String name: folder.list( filter))
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

  private String path;
  private String prefix;
  private int maxCount;
  private long maxSize;
  private long size;
  private List<String> files;
  private OutputStream stream;
  private BlockingQueue<String> queue;
  private Thread thread;
}
