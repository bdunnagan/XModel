package org.xmodel.net;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.jboss.netty.bootstrap.ClientBootstrap;
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
   * @param bootstrap The netty client bootstrap.
   * @param address The remote address.
   * @param scheduler The scheduler for scheduling retries.
   * @param retries The maximum number of retry attempts.
   * @param delays An array of delays between retries in milliseconds.
   */
  public ConnectFuture( ClientBootstrap bootstrap, SocketAddress address, ScheduledExecutorService scheduler, int retries, int[] delays)
  {
    if ( retries < 0) throw new IllegalArgumentException( "retries < 0");
    if ( delays.length == 0) throw new IllegalArgumentException( "delays.length == 0");
    
    this.bootstrap = bootstrap;
    this.address = address;
    this.scheduler = scheduler;
    this.cancelled = new AtomicBoolean( false);
    this.done = new AtomicBoolean( false);
    this.retries = retries;
    this.delays = delays;
    this.semaphore = new Semaphore( 0);
    this.listeners = new ArrayList<ChannelFutureListener>( 3);

    // make first connection attempt
    ChannelFuture future = bootstrap.connect( address);
    this.delegate = new AtomicReference<ChannelFuture>( future);
    
    future.addListener( delegateListener);
  }
  
  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#addListener(org.jboss.netty.channel.ChannelFutureListener)
   */
  @Override
  public void addListener( ChannelFutureListener listener)
  {
    if ( done.get())
    {
      delegate.get().addListener( listener);
    }
    else
    {
      if ( !listeners.contains( listener)) listeners.add( listener);
    }
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
    return done.get();
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
  
  /**
   * @param attempt The current retry attempt.
   * @return Returns the current retry delay.
   */
  private int getRetryDelay( int attempt)
  {
    if ( attempt >= delays.length) attempt = delays.length - 1;
    int delay = delays[ attempt];
    if ( delay < 0) delay = 0;
    return delay;
  }
    
  private ChannelFutureListener delegateListener = new ChannelFutureListener() {
    public void operationComplete( ChannelFuture future) throws Exception
    {
      System.out.printf( "attempt=%d, retries=%d\n", attempt+1, retries);
      if ( !future.isSuccess() && !future.isCancelled() && ++attempt < retries)
      {
        int delay = getRetryDelay( attempt - 1);
        System.out.printf( "delay=%d\n", delay);
        scheduler.schedule( retryRunnable, delay, TimeUnit.MILLISECONDS);
      }
      else
      {
        for( ChannelFutureListener listener: listeners)
          listener.operationComplete( future);
        
        done.set( true); 
        semaphore.release();
      }
    }
  };
  
  private Runnable retryRunnable = new Runnable() {
    public void run()
    {
      ChannelFuture future = bootstrap.connect( address);
      delegate.set( future);
      future.addListener( delegateListener);
    }
  };

  private ClientBootstrap bootstrap;
  private SocketAddress address;
  private ScheduledExecutorService scheduler;
  private AtomicReference<ChannelFuture> delegate;
  private Semaphore semaphore;
  private AtomicBoolean cancelled;
  private AtomicBoolean done;
  private List<ChannelFutureListener> listeners;
  private int retries;
  private int attempt;
  private int[] delays;
}
