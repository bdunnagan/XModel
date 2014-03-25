package org.xmodel.net.protocol.execute;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.xmodel.IModelObject;
import org.xmodel.NullObject;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.future.AsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.net.HeaderProtocol.Type;
import org.xmodel.net.IXioCallback;
import org.xmodel.net.XioChannel;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

public class ExecutionResponseProtocol
{
  public ExecutionResponseProtocol( ExecutionProtocol bundle)
  {
    this.bundle = bundle;
    this.queues = new ConcurrentHashMap<Long, BlockingQueue<IModelObject>>();
    this.tasks = new ConcurrentHashMap<Long, ResponseTask>();
  }

  /**
   * Reset this instance by releasing internal resources.  This method should be called after 
   * the channel is closed to prevent conflict between protocol traffic and the freeing of resources.
   */
  public void reset()
  {
    Iterator< Map.Entry<Long, BlockingQueue<IModelObject>>> iter = queues.entrySet().iterator();
    while( iter.hasNext())
    {
      Map.Entry<Long, BlockingQueue<IModelObject>> entry = iter.next();
      iter.remove();
      if ( entry.getValue() != null) entry.getValue().offer( closedQueueIndicator);
    }
    
    Iterator< Map.Entry<Long, ResponseTask>> taskIter = tasks.entrySet().iterator();
    while( iter.hasNext())
    {
      Map.Entry<Long, ResponseTask> entry = taskIter.next();
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
  public void send( XioChannel channel, long correlation, IContext context, Object[] results) throws IOException
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
  public void send( XioChannel channel, long correlation, IContext context, Throwable throwable) throws IOException, InterruptedException
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
  public void handle( XioChannel channel, ChannelBuffer buffer) throws IOException
  {
    Object correlation = channel.getProtocol().getCorrelation( buffer);
    
//    ICompressor compressor = (bundle.requestCompressor != null)? bundle.requestCompressor: new TabularCompressor( false);
//    IModelObject response = compressor.decompress( new ChannelBufferInputStream( buffer));
//    
//    BlockingQueue<IModelObject> queue = queues.get( correlation);
//    log.debugf( "ExecutionResponseProtocol.handle: corr=%d, queue? %s", correlation, (queue != null)? "yes": "no");
//    if ( queue != null) queue.offer( response);
//    
//    ResponseTask task = tasks.remove( correlation);
//    log.debugf( "ExecutionResponseProtocol.handle: corr=%d, task? %s", correlation, (task != null)? "yes": "no");
//    if ( task != null && task.cancelTimer()) 
//    {
//      task.setResponse( response);
//      Executor executor = task.context.getExecutor();
//      executor.execute( task);
//    }
  }
  
  /**
   * Handle an execution request timeout.
   * @param task The response task for the request.
   * @param correlation The request correlation number.
   */
  protected void handleTimeout( ResponseTask task, long correlation)
  {
    log.debug( "Response timeout.");
    task.setError( "timeout");
    tasks.remove( correlation);
    task.context.getExecutor().execute( task);
  }
  
  public static class ResponseTask implements Runnable
  {
    public ResponseTask( XioChannel channel, IContext context, IXioCallback callback)
    {
      this.channel = channel;
      this.context = context;
      this.callback = callback;
      
      closeListener = new AsyncFuture.IListener<XioChannel>() {
        public void notifyComplete( AsyncFuture<XioChannel> future) throws Exception
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
      log.debug( "Execution timer set");
      this.timer = timer;
    }
    
    /**
     * Cancel timer and true if timer has not already fired.
     * @return Returns true if cancellation was successful and timer has not already fired.
     */
    public boolean cancelTimer()
    {
      log.debug( "Execution timer cancelled");
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

    private XioChannel channel;
    private AsyncFuture.IListener<XioChannel> closeListener;
    private IContext context;
    private IXioCallback callback;
    private ScheduledFuture<?> timer;
    private IModelObject response;
    private String error;
  }
  
  private final static Log log = Log.getLog( ExecutionResponseProtocol.class);
  private final static IModelObject closedQueueIndicator = new NullObject();

  private ExecutionProtocol bundle;
  private Map<Long, BlockingQueue<IModelObject>> queues;
  private Map<Long, ResponseTask> tasks;
}
