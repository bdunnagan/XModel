package org.xmodel.log;

/**
 * An implementation of Log.ISink that logs to the console with a timestamp.
 */
public final class ConsoleSink implements ILogSink
{
  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String)
   */
  @Override
  public void log( Log log, int level, String message)
  {
    System.out.println( message);
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.Throwable)
   */
  @Override
  public void log( Log log, int level, Throwable throwable)
  {
    System.out.println( throwable.toString());
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String, java.lang.Throwable)
   */
  @Override
  public void log( Log log, int level, String message, Throwable throwable)
  {
    System.out.println( message);
    System.out.println( throwable.toString());
  }
}
