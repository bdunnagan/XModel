package org.xmodel.net;

import java.util.concurrent.TimeUnit;

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
   * @param future The future.
   */
  public ConnectFuture( ChannelFuture future)
  {
    delegate = future;
    delegate.addListener( arg0)
  }
  
  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#addListener(org.jboss.netty.channel.ChannelFutureListener)
   */
  @Override
  public void addListener( ChannelFutureListener arg0)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#await()
   */
  @Override
  public ChannelFuture await() throws InterruptedException
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#await(long, java.util.concurrent.TimeUnit)
   */
  @Override
  public boolean await( long arg0, TimeUnit arg1) throws InterruptedException
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#await(long)
   */
  @Override
  public boolean await( long arg0) throws InterruptedException
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#awaitUninterruptibly()
   */
  @Override
  public ChannelFuture awaitUninterruptibly()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#awaitUninterruptibly(long, java.util.concurrent.TimeUnit)
   */
  @Override
  public boolean awaitUninterruptibly( long arg0, TimeUnit arg1)
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#awaitUninterruptibly(long)
   */
  @Override
  public boolean awaitUninterruptibly( long arg0)
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#cancel()
   */
  @Override
  public boolean cancel()
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#getCause()
   */
  @Override
  public Throwable getCause()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#getChannel()
   */
  @Override
  public Channel getChannel()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#isCancelled()
   */
  @Override
  public boolean isCancelled()
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#isDone()
   */
  @Override
  public boolean isDone()
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#isSuccess()
   */
  @Override
  public boolean isSuccess()
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#removeListener(org.jboss.netty.channel.ChannelFutureListener)
   */
  @Override
  public void removeListener( ChannelFutureListener arg0)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#rethrowIfFailed()
   */
  @Override
  public ChannelFuture rethrowIfFailed() throws Exception
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#setFailure(java.lang.Throwable)
   */
  @Override
  public boolean setFailure( Throwable arg0)
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#setProgress(long, long, long)
   */
  @Override
  public boolean setProgress( long arg0, long arg1, long arg2)
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#setSuccess()
   */
  @Override
  public boolean setSuccess()
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#sync()
   */
  @Override
  public ChannelFuture sync() throws InterruptedException
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.jboss.netty.channel.ChannelFuture#syncUninterruptibly()
   */
  @Override
  public ChannelFuture syncUninterruptibly()
  {
    // TODO Auto-generated method stub
    return null;
  }

  private ChannelFuture delegate;
}
