package org.xmodel.future;

import org.xmodel.log.Log;

/**
 * An AsyncFuture which is created in the successful state.
 */
public class SuccessAsyncFuture<T> extends AsyncFuture<T>
{
  public SuccessAsyncFuture( T initiator)
  {
    super( initiator);
    try
    {
      notifySuccess();
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
  
  private final static Log log = Log.getLog( SuccessAsyncFuture.class);
}
