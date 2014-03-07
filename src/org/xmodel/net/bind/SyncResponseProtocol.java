package org.xmodel.net.bind;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.HeaderProtocol.Type;
import org.xmodel.net.IXioChannel;

public class SyncResponseProtocol
{
  public SyncResponseProtocol( BindProtocol bundle)
  {
    this.bundle = bundle;
    this.queues = new ConcurrentHashMap<Integer, BlockingQueue<IModelObject>>();
  }
  
  /**
   * Reset this instance by releasing internal resources.  This method should be called after 
   * the channel is closed to prevent conflict between protocol traffic and the freeing of resources.
   */
  public void reset()
  {
    queues.clear();
  }
  
  /**
   * Send a sync response.
   * @param channel The channel.
   * @param correlation The correlation number.
   * @param element Null or the element identified by the bind request query.
   */
  public void send( IXioChannel channel, int correlation, IModelObject element) throws IOException
  {
    log.debugf( "SyncResponseProtocol.send: corr=%d, found=%s", correlation, (element != null)? "true": "false");
    
    List<byte[]> buffers = bundle.responseCompressor.compress( element);
    ChannelBuffer buffer2 = ChannelBuffers.wrappedBuffer( buffers.toArray( new byte[ 0][]));
    ChannelBuffer buffer1 = bundle.headerProtocol.writeHeader( 4, Type.syncResponse, buffer2.readableBytes());
    buffer1.writeInt( correlation);
    
    // ignoring write buffer overflow for this type of messaging
    channel.write( ChannelBuffers.wrappedBuffer( buffer1, buffer2));
  }
  
  /**
   * Handle the next bind response message in the specified buffer.
   * @param channel The channel.
   * @param buffer The buffer.
   */
  public void handle( IXioChannel channel, ChannelBuffer buffer) throws IOException
  {
    int correlation = buffer.readInt();
    IModelObject element = bundle.requestCompressor.decompress( new ChannelBufferInputStream( buffer));
    
    log.debugf( "SyncResponseProtocol.handle: corr=%d, element=%s", correlation, element.getType());
    
    BlockingQueue<IModelObject> queue = queues.get( correlation);
    if ( queue != null) queue.offer( element); 
  }

  /**
   * Allocates the next correlation number.
   * @return Returns the correlation number.
   */
  protected int nextCorrelation()
  {
    int correlation = bundle.headerProtocol.correlation();
    queues.put( correlation, new ArrayBlockingQueue<IModelObject>( 1));
    return correlation;
  }
  
  /**
   * Wait for a response to the request with the specified correlation number.
   * @param correlation The correlation number.
   * @param timeout The timeout in milliseconds.
   * @return Returns null or the response.
   */
  protected IModelObject waitForResponse( long correlation, int timeout) throws InterruptedException
  {
    try
    {
      BlockingQueue<IModelObject> queue = queues.get( correlation);
      return queue.poll( timeout, TimeUnit.MILLISECONDS);
    }
    finally
    {
      queues.remove( correlation);
    }
  }

  private final static Log log = Log.getLog( SyncResponseProtocol.class);

  private BindProtocol bundle;
  private Map<Integer, BlockingQueue<IModelObject>> queues;
}
