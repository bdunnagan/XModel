package org.xmodel.future;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.xmodel.future.AsyncFuture.IListener;
import org.xmodel.log.Log;

/**
 * An implementation of AsyncFuture<T> that provides notification after its subordinate
 * AsyncFuture<T> tasks completes.  This future is always successful.
 */
public class UnionFuture<T, U> extends AsyncFuture<T> implements IListener<U>
{
  public UnionFuture( T initiator)
  {
    super( initiator);
    tasks = Collections.synchronizedList( new ArrayList<AsyncFuture<U>>());
    completed = new AtomicInteger( 0);
  }

  /* (non-Javadoc)
   * @see com.nephos6.ip6sonar.webclient.AsyncFuture#cancel()
   */
  @Override
  public synchronized void cancel()
  {
    for( AsyncFuture<U> task: tasks)
      task.cancel();
  }

  /**
   * Add a subordinate task to this future.
   * @param future The task future.
   */
  public void addTask( final AsyncFuture<U> future)
  {
    log.debugf( "addTask( %x, %x)", hashCode(), future.hashCode());
    
    tasks.add( future);
    future.addListener( this);
  }
  
  /**
   * @return Returns the subordinate tasks.
   */
  public List<AsyncFuture<U>> getTasks()
  {
    return tasks;
  }

  /* (non-Javadoc)
   * @see com.nephos6.ip6sonar.webclient.AsyncFuture.IListener#notifyComplete(com.nephos6.ip6sonar.webclient.AsyncFuture)
   */
  @Override
  public void notifyComplete( AsyncFuture<U> future) throws Exception
  {
    if ( completed.incrementAndGet() == tasks.size())
      UnionFuture.this.notifySuccess();
  }
    
  private final static Log log = Log.getLog( UnionFuture.class);
  
  private List<AsyncFuture<U>> tasks;
  private AtomicInteger completed;
}
