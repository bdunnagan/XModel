package org.xmodel;

import org.xmodel.log.SLog;

/**
 * Watchdog thread that will log a stack trace for a target thread if not petted within a specified timeout.
 */
public class Watchdog extends Thread
{
  public Watchdog( int timeout)
  {
    super( String.format( "Watchdog [%s]", Thread.currentThread().getName()));
    
    this.thread = Thread.currentThread();
    this.timeout = timeout;
    
    setDaemon( true);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Thread#start()
   */
  @Override
  public synchronized void start()
  {
    pet();
    super.start();
  }

  /**
   * Reset the timer on the watchdog.
   */
  public void pet()
  {
    timestamp = System.currentTimeMillis();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run()
  {
    while( true)
    {
      try
      {
        Thread.sleep( timeout);
        
        long now = System.currentTimeMillis();
        long elapsed = now - timestamp;
        if ( elapsed > timeout) logStackTrace( elapsed);
      }
      catch( InterruptedException e)
      {
        break;
      }
    }
  }
  
  /**
   * Dump the stack of the thread.
   * @param elapsed Elapsed time in milliseconds since last pet.
   */
  private void logStackTrace( long elapsed)
  {
    StringBuilder sb = new StringBuilder();
    StackTraceElement[] trace = thread.getStackTrace();
    for( int i=0; i<trace.length; i++)
    {
      String file = String.format( "%s:%d", 
        trace[ i].getFileName(),
        trace[ i].getLineNumber());
      
      String line = String.format( "%30s - %s.%s\n", 
        file,
        trace[ i].getClassName(),
        trace[ i].getMethodName());
      sb.append( line);
    }
    
    SLog.warnf( this, "Watchdog has not been petted for %d seconds - Target Stack:\n%s", (elapsed / 1000), sb);
  }
  
  private Thread thread;
  private int timeout;
  private volatile long timestamp;
}