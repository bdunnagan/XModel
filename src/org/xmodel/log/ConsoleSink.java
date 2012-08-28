package org.xmodel.log;

import org.xmodel.IModelObject;

/**
 * An implementation of Log.ISink that logs to the console with a timestamp.
 */
public final class ConsoleSink implements ILogSink
{
  /* (non-Javadoc)
   * @see org.xmodel.log.ILogSink#configure(org.xmodel.IModelObject)
   */
  @Override
  public void configure( IModelObject config)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String)
   */
  @Override
  public void log( Log log, int level, Object message)
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
  public void log( Log log, int level, Object message, Throwable throwable)
  {
    System.out.printf( "%s\n%s\n", message, throwable.toString());
  }
}
