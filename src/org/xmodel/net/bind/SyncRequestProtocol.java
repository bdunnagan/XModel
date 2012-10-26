package org.xmodel.net.bind;

import java.io.IOException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.FullProtocolChannelHandler.Type;

public class SyncRequestProtocol
{
  public SyncRequestProtocol( BindProtocol bundle)
  {
    this.bundle = bundle;
  }
  
  /**
   * Send a sync request.
   * @param channel The channel.
   * @param key The network identifier.
   * @param timeout The timeout in milliseconds.
   * @return Returns null or the result of the sync.
   */
  public IModelObject send( Channel channel, long netID, int timeout) throws InterruptedException
  {
    int correlation = bundle.syncResponseProtocol.nextCorrelation();
    log.debugf( "SyncRequestProtocol.send (sync): corr=%d, timeout=%d, netID=%X", correlation, timeout, netID);

    ChannelBuffer buffer = bundle.headerProtocol.writeHeader( Type.syncRequest, 12);
    buffer.writeInt( correlation);
    buffer.writeLong( netID);
    
    // ignoring write buffer overflow for this type of messaging
    channel.write( buffer);
    
    return bundle.syncResponseProtocol.waitForResponse( correlation, timeout);
  }

  /**
   * Handle a sync request.
   * @param channel The channel.
   * @param buffer The buffer.
   */
  public void handle( Channel channel, ChannelBuffer buffer)
  {
    int correlation = buffer.readInt();
    long netID = buffer.readLong();
    
    bundle.dispatcher.execute( new SyncRunnable( channel, correlation, netID));
  }
  
  /**
   * Serve the sync request.
   * @param channel The channel.
   * @param correlation The correlation.
   * @param netID The network identifier.
   */
  private void sync( Channel channel, int correlation, long netID)
  {
    IModelObject element = bundle.serverCompressor.findLocal( netID);
    
    UpdateListener listener = bundle.bindRequestProtocol.getListener( element);
    if ( listener == null)
    {
      log.errorf( "UpdateListener not found for element %X", netID);
      return;
    }
    
    try
    {
      // disable updates
      listener.setEnabled( false);
      
      // sync
      element.getChildren();
      
      // send response
      try
      {
        bundle.syncResponseProtocol.send( channel, correlation, element);
      }
      catch( IOException e)
      {
        log.exceptionf( e, "Failed to send sync response for %X", netID);
      }
    }
    finally
    {
      listener.setEnabled( true);
    }
  }
  
  private class SyncRunnable implements Runnable
  {
    public SyncRunnable( Channel channel, int correlation, long netID)
    {
      this.channel = channel;
      this.correlation = correlation;
      this.netID = netID;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
      sync( channel, correlation, netID);
    }
    
    private Channel channel;
    private int correlation;
    private long netID;
  }
  
  private final static Log log = Log.getLog( SyncResponseProtocol.class);

  private BindProtocol bundle;
}
