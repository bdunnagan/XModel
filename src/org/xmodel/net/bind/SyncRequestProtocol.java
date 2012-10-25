package org.xmodel.net.bind;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.xmodel.external.IExternalReference;
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
   * @param reference The reference to be updated.
   * @param timeout The timeout in milliseconds.
   */
  public void send( Channel channel, long netID, IExternalReference reference, int timeout)
  {
    int correlation = bundle.syncResponseProtocol.nextCorrelation();
    log.debugf( "SyncRequestProtocol.send (sync): corr=%d, timeout=%d, netID=%X", correlation, timeout, netID);

    ChannelBuffer buffer = bundle.headerProtocol.writeHeader( Type.syncRequest, 12);
    buffer.writeInt( correlation);
    buffer.writeLong( netID);
    
    // ignoring write buffer overflow for this type of messaging
    channel.write( buffer);
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
