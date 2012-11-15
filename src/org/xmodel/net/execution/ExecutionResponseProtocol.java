package org.xmodel.net.execution;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.XioChannelHandler.Type;
import org.xmodel.net.IXioCallback;
import org.xmodel.net.XioExecutionException;
import org.xmodel.xpath.expression.IContext;

public class ExecutionResponseProtocol
{
  public ExecutionResponseProtocol( ExecutionProtocol bundle)
  {
    this.bundle = bundle;
    this.counter = new AtomicInteger( 1);
    this.queues = new ConcurrentHashMap<Integer, SynchronousQueue<IModelObject>>();
    this.tasks = new ConcurrentHashMap<Integer, ResponseTask>();
  }

  /**
   * Reset this instance by releasing internal resources.  This method should be called after 
   * the channel is closed to prevent conflict between protocol traffic and the freeing of resources.
   */
  public void reset()
  {
    queues.clear();
    tasks.clear();
  }
  
  /**
   * Send an execution response.
   * @param channel The channel.
   * @param correlation The correlation.
   * @param context The execution context.
   * @param results The execution results.
   */
  public void send( Channel channel, int correlation, IContext context, Object[] results) throws IOException
  {
    log.debugf( "ExecutionResponseProtocol.send: corr=%d", correlation);
    
    IModelObject response = ExecutionSerializer.buildResponse( context, results);
    ChannelBuffer buffer2 = bundle.downstreamCompressor.compress( response);
    
    ChannelBuffer buffer1 = bundle.headerProtocol.writeHeader( Type.executeResponse, buffer2.readableBytes());
    buffer1.writeInt( correlation);
    
    // ignoring write buffer overflow for this type of messaging
    channel.write( ChannelBuffers.wrappedBuffer( buffer1, buffer2));
  }
  
  /**
   * Send an execution response.
   * @param channel The channel.
   * @param correlation The correlation.
   * @param context The execution context.
   * @param throwable An exception thrown during remote execution.
   */
  public void send( Channel channel, int correlation, IContext context, Throwable throwable) throws IOException, InterruptedException
  {
    log.debugf( "ExecutionResponseProtocol.send: corr=%d, exception=%s: %s", correlation, throwable.getClass().getName(), throwable.getMessage());
    
    IModelObject response = ExecutionSerializer.buildResponse( context, throwable);
    ChannelBuffer buffer2 = bundle.downstreamCompressor.compress( response);
    
    ChannelBuffer buffer1 = bundle.headerProtocol.writeHeader( Type.executeResponse, buffer2.readableBytes());
    buffer1.writeInt( correlation);
    
    // ignoring write buffer overflow for this type of messaging
    channel.write( ChannelBuffers.wrappedBuffer( buffer1, buffer2));
  }
  
  /**
   * Handle a response.
   * @param channel The channel.
   * @param buffer The buffer.
   */
  public void handle( Channel channel, ChannelBuffer buffer) throws IOException
  {
    int correlation = buffer.readInt();
    
    IModelObject response = bundle.upstreamCompressor.decompress( buffer);
    
    SynchronousQueue<IModelObject> queue = queues.remove( correlation);
    if ( queue != null) queue.offer( response);
    
    ResponseTask task = tasks.remove( correlation);
    if ( task != null && !task.isExpired()) task.run();
  }
  
  /**
   * Allocates the next correlation number.
   * @return Returns the correlation number.
   */
  protected int nextCorrelation()
  {
    int correlation = counter.getAndIncrement();
    queues.put( correlation, new SynchronousQueue<IModelObject>());
    return correlation;
  }
  
  /**
   * Allocates the next correlation number and associates the specified callback.
   * @param callback The callback.
   * @return Returns the correlation number.
   */
  protected int nextCorrelation( ResponseTask runnable)
  {
    int correlation = counter.getAndIncrement();
    tasks.put( correlation, runnable);
    return correlation;
  }
  
  /**
   * Wait for a response to the request with the specified correlation number.
   * @param correlation The correlation number.
   * @param timeout The timeout in milliseconds.
   * @return Returns null or the response.
   */
  protected Object[] waitForResponse( int correlation, int timeout) throws InterruptedException, XioExecutionException
  {
    try
    {
      SynchronousQueue<IModelObject> queue = queues.get( correlation);
      IModelObject response = queue.poll( timeout, TimeUnit.MILLISECONDS);
      
      Throwable throwable = ExecutionSerializer.readResponseException( response);
      if ( throwable != null) throw new XioExecutionException( "Remote invocation exception", throwable);
      
      return ExecutionSerializer.readResponse( response, bundle.context);
    }
    finally
    {
      queues.remove( correlation);
    }
  }

  public static class ResponseTask implements Runnable
  {
    public ResponseTask( IContext context, IXioCallback callback)
    {
      this.context = context;
      this.callback = callback;
    }
    
    /**
     * Set the timer.
     * @param timer The timer.
     */
    public void setTimer( ScheduledFuture<?> timer)
    {
      this.timer = timer;
    }
    
    /**
     * Test whether this task has expired.
     * @return Returns true if task has expired.
     */
    public boolean isExpired()
    {
      return timer != null && !timer.cancel( false);
    }
    
    /**
     * Set the error string.
     * @param error The error string.
     */
    public void setError( String error)
    {
      this.error = error;
    }
    
    /**
     * Set the response element. 
     * @param response The response element.
     */
    public void setResponse( IModelObject response)
    {
      this.response = response;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {      
      callback.onComplete( context);
      
      if ( response != null)
      {
        Throwable throwable = ExecutionSerializer.readResponseException( response);
        if ( throwable != null)
        {
          log.exceptionf( throwable, "Remote invocation returned exception: ");
          error = String.format( "%s: %s", throwable.getClass().getName(), throwable.getMessage());
        }
        else
        {
          Object[] results = ExecutionSerializer.readResponse( response, context);
          callback.onSuccess( context, results); 
        }
      }

      if ( error != null)
      {
        callback.onError( context, error);
      }
    }

    private IContext context;
    private IXioCallback callback;
    private ScheduledFuture<?> timer;
    private IModelObject response;
    private String error;
  }
  
  private final static Log log = Log.getLog( ExecutionResponseProtocol.class);

  private ExecutionProtocol bundle;
  private AtomicInteger counter;
  private Map<Integer, SynchronousQueue<IModelObject>> queues;
  private Map<Integer, ResponseTask> tasks;
}
