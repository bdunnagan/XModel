package org.xmodel.log;

import java.util.Calendar;

/**
 * An implementation of Log.ISink performs basic formatting and delegates logging.
 */
public final class FormatSink implements ILogSink
{
  public FormatSink( ILogSink delegate)
  {
    this.delegate = delegate;
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String)
   */
  @Override
  public void log( Log log, int level, Object message)
  {
    String thread = Thread.currentThread().getName();
    StringBuilder sb = new StringBuilder();
    formatDate( Calendar.getInstance(), sb);
    sb.append( " ["); sb.append( thread); sb.append( "] ");
    sb.append( Log.getLevelName( level));
    sb.append( " ("); sb.append( log.getName()); sb.append( ") - ");
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
  public void log( Log log, int level, Object object, Throwable throwable)
  {
    String message = object.toString();
    String levelName = Log.getLevelName( level);

    StringBuilder date = new StringBuilder();
    formatDate( Calendar.getInstance(), date);
    
    StringBuilder sb = new StringBuilder();
    
    if ( message.length() > 0)
    {
      sb.append( date); sb.append( ' ');
      sb.append( levelName);
      sb.append( " ("); sb.append( log.getName()); sb.append( ") - ");
      sb.append( message);
      delegate.log( log, level, sb.toString());
    }
    
    sb.setLength( 0);
    sb.append( date); sb.append( ' ');
    sb.append( levelName);
    sb.append( " ("); sb.append( log.getName()); sb.append( ") - ");
    sb.append( throwable.getClass().getSimpleName());
    sb.append( ": ");
    sb.append( throwable.getMessage());
    delegate.log( log, level, sb.toString());
    
    StackTraceElement[] stack = throwable.getStackTrace();
    for( StackTraceElement element: stack)
    {
      sb.setLength( 0);
      sb.append( date); sb.append( ' ');
      sb.append( levelName);
      sb.append( " ("); sb.append( log.getName()); sb.append( ") - ");
      sb.append( element.toString());
      delegate.log( log, level, sb.toString());
    }
  }

  /**
   * Format the specified absolute time into the specified message.
   * @param calendar The calendar instance.
   * @param message The message.
   */
  private void formatDate( Calendar calendar, StringBuilder message)
  {
    int year = calendar.get( Calendar.YEAR);
    int month = calendar.get( Calendar.MONTH);
    int day = calendar.get( Calendar.DAY_OF_MONTH);
    
    int hour = calendar.get( Calendar.HOUR);
    int min = calendar.get( Calendar.MINUTE);
    int sec = calendar.get( Calendar.SECOND);
    int msec = calendar.get( Calendar.MILLISECOND);
    
//    message.append( Integer.toString( year)); message.append( '/');
//    message.append( Integer.toString( month)); message.append( '/');
//    message.append( Integer.toString( day)); message.append( ' ');
    
    message.append( String.format( "%02d:%02d:%02d.%03d", hour, min, sec, msec));
  }
  
  private ILogSink delegate;
}
