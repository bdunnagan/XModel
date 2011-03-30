package org.xmodel.log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * An implementation of Log.ISink performs basic formatting and delegates logging.
 */
public final class FormatSink implements ILogSink
{
  public FormatSink( ILogSink delegate)
  {
    this.delegate = delegate;
    calendar = Calendar.getInstance();
    dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS");
    dateFormat.setCalendar( calendar);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String)
   */
  @Override
  public void log( Log log, int level, String message)
  {
    StringBuilder sb = new StringBuilder();
    sb.append( Log.getLevelName( level)); sb.append( ' ');
    sb.append( log.getName()); sb.append( ' ');
    sb.append( dateFormat.format( new Date())); sb.append( ' ');
    sb.append( message);
    delegate.log( log, level, sb.toString());
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.Throwable)
   */
  @Override
  public void log( Log log, int level, Throwable throwable)
  {
    log( log, level, "", throwable);
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String, java.lang.Throwable)
   */
  @Override
  public void log( Log log, int level, String message, Throwable throwable)
  {
    String levelName = Log.getLevelName( level);
    
    StringBuilder sb = new StringBuilder();
    sb.append( levelName); sb.append( ' ');
    sb.append( log.getName()); sb.append( ' ');
    sb.append( dateFormat.format( new Date())); sb.append( ' ');
    sb.append( message);
    delegate.log( log, level, message);
    
    StackTraceElement[] stack = throwable.getStackTrace();
    for( StackTraceElement element: stack)
    {
      sb.setLength( 0);
      sb.append( levelName); sb.append( ' ');
      sb.append( log.getName()); sb.append( ' ');
      sb.append( "                        ");
      sb.append( element.toString());
      delegate.log( log, level, sb.toString());
    }
  }

  private ILogSink delegate;
  private Calendar calendar;
  private SimpleDateFormat dateFormat;
}
