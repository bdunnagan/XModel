package org.xmodel.log;

import java.util.Calendar;

/**
 * An implementation of Log.ISink performs basic formatting and delegates logging.
 */
public final class FormatSink extends MultiSink
{
  public FormatSink()
  {
  }
  
  public FormatSink( ILogSink... delegates)
  {
    super( delegates);
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String)
   */
  @Override
  public void log( Log log, int level, Object message)
  {
    StringBuilder sb = new StringBuilder();
    formatDate( Calendar.getInstance(), sb);
    sb.append( ' ');
    
    sb.append( Log.getLevelName( level).toUpperCase());
    sb.append( ' ');
    
    String thread = Thread.currentThread().getName();
    sb.append( "["); sb.append( thread); sb.append( "] ");
    
    //sb.append( "("); sb.append( log.getName()); sb.append( ") - ");
    sb.append( "("); sb.append( getStack()); sb.append( ") - ");
    
    sb.append( message);
    
    super.log( log, level, sb.toString());
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
    String thread = Thread.currentThread().getName();
    String levelName = Log.getLevelName( level).toUpperCase();

    String trace = getStack();
    StringBuilder date = new StringBuilder();
    formatDate( Calendar.getInstance(), date);
    
    StringBuilder sb = new StringBuilder();
    
    if ( message.length() > 0)
    {
      sb.append( date); sb.append( ' ');
      sb.append( levelName);
      sb.append( " ["); sb.append( thread); sb.append( "]");
      sb.append( " ("); sb.append( trace); sb.append( ") - ");
      sb.append( message);
      sb.append( '\n');
    }
    
    sb.append( date); sb.append( ' ');
    sb.append( levelName);
    sb.append( " ["); sb.append( thread); sb.append( "]");
    sb.append( " ("); sb.append( trace); sb.append( ") - ");
    sb.append( throwable);
    sb.append( '\n');
    
    StackTraceElement[] stack = throwable.getStackTrace();
    for( StackTraceElement element: stack)
    {
      sb.append( element.toString());
      sb.append( '\n');
    }
    
    super.log( log, level, sb.toString());
  }

  /**
   * Format the specified absolute time into the specified message.
   * @param calendar The calendar instance.
   * @param message The message.
   */
  private void formatDate( Calendar calendar, StringBuilder message)
  {
    int year = calendar.get( Calendar.YEAR) - 2000;
    int month = calendar.get( Calendar.MONTH) + 1;
    int day = calendar.get( Calendar.DAY_OF_MONTH);
    
    int hour = calendar.get( Calendar.HOUR_OF_DAY);
    int min = calendar.get( Calendar.MINUTE);
    int sec = calendar.get( Calendar.SECOND);
    int msec = calendar.get( Calendar.MILLISECOND);
    
    if ( month < 10) message.append( '0');
    message.append( Integer.toString( month)); message.append( '-');
    
    if ( day < 10) message.append( '0');
    message.append( Integer.toString( day)); message.append( '-');
    
    if ( year < 10) message.append( '0');
    message.append( Integer.toString( year)); message.append( ' ');
    
    if ( hour < 10) message.append( '0');
    message.append( Integer.toString( hour)); message.append( ':');
    
    if ( min < 10) message.append( '0');
    message.append( Integer.toString( min)); message.append( ':');
    
    if ( sec < 10) message.append( '0');
    message.append( Integer.toString( sec)); message.append( '.');
    
    if ( msec < 10) message.append( "00");
    else if ( msec < 100) message.append( '0');
    message.append( Integer.toString( msec));
  }
  
  /**
   * @return Return partial stack trace.
   */
  private String getStack()
  {
    // get stack trace
    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    
    // find first non-logging package stack frame
    int start = 1;
    for( ; start < stack.length; start++)
    {
      String className = stack[ start].getClassName();
      if ( !className.startsWith( "org.xmodel.log")) break;
    }

    String className = stack[ start].getClassName();
    int index = className.lastIndexOf( '.');
    if ( index > 0) index = className.lastIndexOf( '.', index-1);
    if ( index > 0) className = className.substring( index+1);

    return className + "." + stack[ start].getMethodName();
  }
}
