package org.xmodel.net;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

/**
 * ConnectFuture wraps the ChannelFuture instance returned when a client connection attempt is made.  It is
 * identical to ChannelFuture with the exception that it may attempt to connect again if a connection attempt
 * fails.  In this case, the underlying ChannelFuture changes with each attempt.
 */
public class ConnectFuture implements ChannelFuture
{
  /**
   * Create with the ChannelFuture for the initial connection attempt.
   * @param address The remote address.
   * @param future The future.
   * @param scheduler The scheduler for scheduling retries.
   * @param retries The maximum number of retry attempts.
   * @param delay The delay between retries in milliseconds.
   * @param backoff A number that is multiplied by the delay after each retry.
   */
  public ConnectFuture( SocketAddress address, ChannelFuture future, ScheduledExecutorService scheduler, int retries, int delay, float backoff)
  {
    if ( backoff < 0) throw new IllegalArgumentException( "backoff < 0");
    
    this.address = address;
    this.scheduler = scheduler;
    this.retries = retries;
    this.delay = delay;
    this.backoff = backoff;
    this.semaphore = new Semaphore( 0);
    this.listeners = new ArrayList<ChannelFutureListener>( 3);
    this.delegate = new AtomicReference<ChannelFuture>( future);
    
    future.addListener( delegateListener);
  }
  
  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#addListener(org.jboss.netty.channel.ChannelFutureListener)
   */
  @Override
  public void addListener( ChannelFutureListener listener)
  {
    if ( !listeners.contains( listener)) listeners.add( listener);
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#removeListener(org.jboss.netty.channel.ChannelFutureListener)
   */
  @Override
  public void removeListener( ChannelFutureListener listener)
  {
    listeners.remove( listener);
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#await()
   */
  @Override
  public ChannelFuture await() throws InterruptedException
  {
    semaphore.acquire();
    return this;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#await(long, java.util.concurrent.TimeUnit)
   */
  @Override
  public boolean await( long timeout, TimeUnit unit) throws InterruptedException
  {
    return semaphore.tryAcquire( timeout, unit);
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#await(long)
   */
  @Override
  public boolean await( long timeout) throws InterruptedException
  {
    return await( timeout, TimeUnit.MILLISECONDS);
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#awaitUninterruptibly()
   */
  @Override
  public ChannelFuture awaitUninterruptibly()
  {
    semaphore.acquireUninterruptibly();
    return this;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#awaitUninterruptibly(long, java.util.concurrent.TimeUnit)
   */
  @Override
  public boolean awaitUninterruptibly( long timeout, TimeUnit unit)
  {
    while( true)
    {
      try
      {
        return semaphore.tryAcquire( timeout, unit);
      }
      catch( InterruptedException e)
      {
      }
    }
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#awaitUninterruptibly(long)
   */
  @Override
  public boolean awaitUninterruptibly( long timeout)
  {
    return awaitUninterruptibly( timeout, TimeUnit.MILLISECONDS);
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#cancel()
   */
  @Override
  public boolean cancel()
  {
    if ( !cancelled.getAndSet( true))
    {
      semaphore.release();
      
      ChannelFuture future = delegate.get();
      if ( future != null) return future.cancel();
    }
    
    return true;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#getCause()
   */
  @Override
  public Throwable getCause()
  {
    ChannelFuture future = delegate.get();
    if ( future != null) return future.getCause();
    return null;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#getChannel()
   */
  @Override
  public Channel getChannel()
  {
    ChannelFuture future = delegate.get();
    if ( future != null) return future.getChannel();
    return null;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#isCancelled()
   */
  @Override
  public boolean isCancelled()
  {
    return cancelled.get();
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#isDone()
   */
  @Override
  public boolean isDone()
  {
    ChannelFuture future = delegate.get();
    if ( future != null) return future.isDone();
    return false;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#isSuccess()
   */
  @Override
  public boolean isSuccess()
  {
    ChannelFuture future = delegate.get();
    if ( future != null) return future.isSuccess();
    return false;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#rethrowIfFailed()
   * @throws UnsupportedOperationException
   */
  @Override
  public ChannelFuture rethrowIfFailed() throws Exception
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#setFailure(java.lang.Throwable)
   * @throws UnsupportedOperationException
   */
  @Override
  public boolean setFailure( Throwable cause)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#setProgress(long, long, long)
   * @throws UnsupportedOperationException
   */
  @Override
  public boolean setProgress( long amount, long current, long total)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#setSuccess()
   * @throws UnsupportedOperationException
   */
  @Override
  public boolean setSuccess()
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#sync()
   * @throws UnsupportedOperationException
   */
  @Override
  public ChannelFuture sync() throws InterruptedException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#syncUninterruptibly()
   * @throws UnsupportedOperationException
   */
  @Override
  public ChannelFuture syncUninterruptibly()
  {
    throw new UnsupportedOperationException();
  }
    
  private ChannelFutureListener delegateListener = new ChannelFutureListener() {
    public void operationComplete( ChannelFuture future) throws Exception
    {
      if ( !future.isSuccess() && !future.isCancelled() && --retries > 0)
      {
        scheduler.schedule( retryRunnable, delay, TimeUnit.MILLISECONDS);
        delay *= backoff;
      }
      else
      {
        semaphore.release();
      }
    }
  };
  
  private Runnable retryRunnable = new Runnable() {
    public void run()
    {
      delegate.set( delegate.get().getChannel().connect( address));
    }
  };

  private SocketAddress address;
  private ScheduledExecutorService scheduler;
  private AtomicReference<ChannelFuture> delegate;
  private Semaphore semaphore;
  private AtomicBoolean cancelled;
  private List<ChannelFutureListener> listeners;
  private int retries;
  private int delay;
  private float backoff;
}
