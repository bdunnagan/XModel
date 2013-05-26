package org.xmodel.future;

import org.xmodel.log.Log;

/**
 * An AsyncFuture which is created in the failure state.
 */
public class FailureAsyncFuture<T> extends AsyncFuture<T>
{
  public FailureAsyncFuture( T initiator, String message)
  {
    super( initiator);
    try
    {
      notifyFailure( message);
    }
    catch( Exception e)
    {
      log.error( "Future notification failed because: ");
      log.exception( e);
    }
  }
  
  public FailureAsyncFuture( T initiator, Throwable throwable)
  {
    super( initiator);
    try
    {
      notifyFailure( throwable);
    }
    catch( Exception e)
    {
      log.error( "Future notification failed because: ");
      log.exception( e);
    }
  }
  
  /* (non-Javadoc)
   * @see com.nephos6.ip6sonar.webclient.AsyncFuture#cancel()
   */
  @Override
  public void cancel()
  {
  }
  
  private final static Log log = Log.getLog( FailureAsyncFuture.class);
}
