package org.xmodel.log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.xmodel.log.Log.ISink;

/**
 * An implementation of Log.ISink that logs to the console with a timestamp.
 */
public final class ConsoleSink implements ISink
{
  public ConsoleSink()
  {
    calendar = Calendar.getInstance();
    dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS");
    dateFormat.setCalendar( calendar);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(int, java.lang.String)
   */
  @Override
  public void log( int level, String message)
  {
    System.out.printf( "%s %s %s\n", Log.getLevelName( level), dateFormat.format( new Date()), message);
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(int, java.lang.Throwable)
   */
  @Override
  public void log( int level, Throwable throwable)
  {
    log( level, "", throwable);
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(int, java.lang.String, java.lang.Throwable)
   */
  @Override
  public void log( int level, String message, Throwable throwable)
  {
    String levelName = Log.getLevelName( level);
    
    System.out.printf( "%s %s %s\n", levelName, dateFormat.format( new Date()), message);
    StackTraceElement[] stack = throwable.getStackTrace();
    for( StackTraceElement element: stack)
    {
      System.out.printf( "%s                         %s\n", 
        levelName, dateFormat.format( new Date()), element.toString());
    }
  }

  private Calendar calendar;
  private SimpleDateFormat dateFormat;
}
