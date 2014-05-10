package org.xmodel.future;

import org.xmodel.future.AsyncFuture.IListener;
import org.xmodel.log.SLog;

/**
 * An implementation of AsyncFuture.IListener that efficiently provides notification
 * when each and all futures in an ordered list of futures have completed.  Note that
 * the future must be added by the client to the first future in the list. 
 */
public class FutureChainListener<T> implements IListener<T>
{
  public FutureChainListener( AsyncFuture<T> ... futures)
  {
    this.futures = futures;
    this.index = 0;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.future.AsyncFuture.IListener#notifyComplete(org.xmodel.future.AsyncFuture)
   */
  @Override
  public final void notifyComplete( AsyncFuture<T> future) throws Exception
  {
    try
    {
      notifyNextComplete( futures, index);
    }
    catch( Exception e)
    {
      SLog.exception( this, e);
    }
    
    if ( ++index < futures.length)
    {
      futures[ index].addListener( this);
    }
    else
    {
      notifyAllComplete( futures);
    } 
  }

  /**
   * Called when the next future completes.
   * @param futures The futures.
   * @param index The index of the completed future.
   */
  protected void notifyNextComplete( AsyncFuture<T>[] futures, int index) throws Exception
  {
  }
  
  /**
   * Called when all futures have completed.
   * @param futures The futures.
   */
  protected void notifyAllComplete( AsyncFuture<T>[] futures) throws Exception
  {
  }
  
  private AsyncFuture<T>[] futures;
  private int index;
}
