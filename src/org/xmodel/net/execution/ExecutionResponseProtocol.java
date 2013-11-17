package org.xmodel.net.execution;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.xmodel.IModelObject;
import org.xmodel.NullObject;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.log.Log;
import org.xmodel.net.IXioCallback;
import org.xmodel.net.XioChannelHandler.Type;
import org.xmodel.net.XioExecutionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

public class ExecutionResponseProtocol
{
  public ExecutionResponseProtocol( ExecutionProtocol bundle)
  {
    this.bundle = bundle;
    this.counter = new AtomicInteger( 1);
    this.queues = new ConcurrentHashMap<Integer, BlockingQueue<IModelObject>>();
    this.tasks = new ConcurrentHashMap<Integer, ResponseTask>();
  }

  /**
   * Reset this instance by releasing internal resources.  This method should be called after 
   * the channel is closed to prevent conflict between protocol traffic and the freeing of resources.
   */
  public void reset()
  {
    Iterator< Map.Entry<Integer, BlockingQueue<IModelObject>>> iter = queues.entrySet().iterator();
    while( iter.hasNext())
    {
      Map.Entry<Integer, BlockingQueue<IModelObject>> entry = iter.next();
      iter.remove();
      if ( entry.getValue() != null) entry.getValue().offer( closedQueueIndicator);
    }
    
    Iterator< Map.Entry<Integer, ResponseTask>> taskIter = tasks.entrySet().iterator();
    while( iter.hasNext())
    {
      Map.Entry<Integer, ResponseTask> entry = taskIter.next();
      taskIter.remove();
      
      ResponseTask task = entry.getValue();
      if ( task != null) 
      {
        task.setError( "Protocol interrupted because connection was closed.");
        Executor executor = task.context.getExecutor();
        executor.execute( task);
      }
    }
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
    
    //
    // The execution protocol can function in a variety of client/server threading models.
    // A progressive, shared TabularCompressor may be used when the worker pool has only one thread.
    //
    ICompressor compressor = (bundle.responseCompressor != null)? bundle.responseCompressor: new TabularCompressor( false);
    
    IModelObject response = ExecutionSerializer.buildResponse( context, results);
    List<byte[]> buffers = compressor.compress( response);
    ChannelBuffer buffer2 = ChannelBuffers.wrappedBuffer( buffers.toArray( new byte[ 0][]));
    ChannelBuffer buffer1 = bundle.headerProtocol.writeHeader( 0, Type.executeResponse, 4 + buffer2.readableBytes(), correlation);
    
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
    
    //
    // The execution protocol can function in a variety of client/server threading models.
    // A progressive, shared TabularCompressor may be used when the worker pool has only one thread.
    //
    ICompressor compressor = (bundle.responseCompressor != null)? bundle.responseCompressor: new TabularCompressor( false);
    
    IModelObject response = ExecutionSerializer.buildResponse( context, throwable);
    List<byte[]> buffers = compressor.compress( response);
    ChannelBuffer buffer2 = ChannelBuffers.wrappedBuffer( buffers.toArray( new byte[ 0][]));
    ChannelBuffer buffer1 = bundle.headerProtocol.writeHeader( 0, Type.executeResponse, 4 + buffer2.readableBytes(), correlation);
    
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
    
    //
    // The execution protocol can function in a variety of client/server threading models.
    // A progressive, shared TabularCompressor may be used when the worker pool has only one thread.
    //
    ICompressor compressor = (bundle.requestCompressor != null)? bundle.requestCompressor: new TabularCompressor( false);
    IModelObject response = compressor.decompress( new ChannelBufferInputStream( buffer));
    
    BlockingQueue<IModelObject> queue = queues.get( correlation);
    log.debugf( "ExecutionResponseProtocol.handle: corr=%d, queue? %s", correlation, (queue != null)? "yes": "no");
    if ( queue != null) queue.offer( response);
    
    ResponseTask task = tasks.remove( correlation);
    log.debugf( "ExecutionResponseProtocol.handle: corr=%d, task? %s", correlation, (task != null)? "yes": "no");
    if ( task != null && task.cancelTimer()) 
    {
      task.setResponse( response);
      Executor executor = task.context.getExecutor();
      executor.execute( task);
    }
  }
  
  /**
   * Handle an execution request timeout.
   * @param task The response task for the request.
   * @param correlation The request correlation number.
   */
  protected void handleTimeout( ResponseTask task, int correlation)
  {
    log.debug( "Response timeout.");
    task.setError( "timeout");
    tasks.remove( correlation);
    bundle.executor.execute( task);
  }
  
  /**
   * Allocates the next correlation without associating a queue or task.
   * @return Returns the allocated correlation number.
   */
  public int allocCorrelation()
  {
    return counter.incrementAndGet();
  }
  
  /**
   * Allocates the next correlation number for a synchronous execution.
   * @return Returns the correlation number.
   */
  protected int nextCorrelation()
  {
    int correlation = counter.incrementAndGet();
    queues.put( correlation, new ArrayBlockingQueue<IModelObject>( 1));
    return correlation;
  }
  
  /**
   * Associate the specified callback with the specified correlation.
   * @param callback The callback.
   * @return Returns the correlation number.
   */
  protected void setCorrelation( int correlation, ResponseTask runnable)
  {
    tasks.put( correlation, runnable);
  }
  
  /**
   * Remove the async context associated with the specified correlation number.
   * @param correlation The correlation number.
   */
  protected void cancel( int correlation)
  {
    tasks.remove( correlation);
  }
  
  /**
   * Wait for a response to the request with the specified correlation number.
   * @param correlation The correlation number.
   * @param context The execution context.
   * @param timeout The timeout in milliseconds.
   * @return Returns null or the response.
   */
  protected Object[] waitForResponse( int correlation, IContext context, int timeout) throws InterruptedException, XioExecutionException
  {
    log.debugf( "waitForResponse: corr=%d", correlation);
    
    try
    {
      BlockingQueue<IModelObject> queue = queues.get( correlation);
      IModelObject response = queue.poll( timeout, TimeUnit.MILLISECONDS);
      
      if ( response == null) 
      {
        log.debugf( "Response is null: corr=%s", correlation);
        return null;
      }
      
      if ( response == closedQueueIndicator)
      {
        log.debugf( "Response interrupted: corr=%s", correlation);
        throw new XioExecutionException( "Protocol interrupted by closed connection.");
      }
        
      Throwable throwable = ExecutionSerializer.readResponseException( response);
      if ( throwable != null) throw new XioExecutionException( "Remote invocation exception", throwable);
      
      return ExecutionSerializer.readResponse( response, context);
    }
    finally
    {
      queues.remove( correlation);
    }
  }

  public static class ResponseTask implements Runnable
  {
    public ResponseTask( Channel channel, IContext context, IXioCallback callback)
    {
      this.channel = channel;
      this.context = context;
      this.callback = callback;
      
      closeListener = new ChannelFutureListener() {
        public void operationComplete( ChannelFuture future) throws Exception
        {
          if ( cancelTimer())
          {
            setError( "Channel closed during remote execution.");
            ResponseTask.this.context.getExecutor().execute( ResponseTask.this);
          }
        }
      };
      
      channel.getCloseFuture().addListener( closeListener);
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
     * Cancel timer and true if timer has not already fired.
     * @return Returns true if cancellation was successful and timer has not already fired.
     */
    public boolean cancelTimer()
    {
      return timer == null || timer.cancel( false);
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
      channel.getCloseFuture().removeListener( closeListener);
      
      IContext context = new StatefulContext( this.context);
      callback.onComplete( context);
      
      if ( response != null)
      {
        Throwable throwable = ExecutionSerializer.readResponseException( response);
        if ( throwable != null)
        {
          log.exceptionf( throwable, "Remote invocation returned exception: ");
          error = String.format( "%s: %s", throwable.getClass().getName(), throwable.getMessage());
          callback.onError( context, error);
        }
        else
        {
          Object[] results = ExecutionSerializer.readResponse( response, context);
          callback.onSuccess( context, results); 
        }
      }
      else if ( error != null)
      {
        callback.onError( context, error);
      }
    }

    private Channel channel;
    private ChannelFutureListener closeListener;
    private IContext context;
    private IXioCallback callback;
    private ScheduledFuture<?> timer;
    private IModelObject response;
    private String error;
  }
  
  private final static Log log = Log.getLog( ExecutionResponseProtocol.class);
  private final static IModelObject closedQueueIndicator = new NullObject();

  private ExecutionProtocol bundle;
  private AtomicInteger counter;
  private Map<Integer, BlockingQueue<IModelObject>> queues;
  private Map<Integer, ResponseTask> tasks;
}
