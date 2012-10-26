package org.xmodel.net.bind;

import java.io.IOException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.FullProtocolChannelHandler.Type;
import org.xmodel.net.nu.ProtocolException;

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
  public void handle( Channel channel, ChannelBuffer buffer) throws ProtocolException
  {
    int correlation = buffer.readInt();
    long netID = buffer.readLong();
    
    IModelObject element = bundle.serverCompressor.findLocal( netID);
    if ( element == null) throw new ProtocolException( String.format( "Element %X not found", netID));
    
    UpdateListener listener = bundle.bindRequestProtocol.getListener( element);
    if ( listener == null) throw new ProtocolException( String.format( "Listener not found on %X", netID));
    
    bundle.dispatcher.execute( new SyncRunnable( channel, correlation, netID, element, listener));
  }
  
  /**
   * Serve the sync request.
   * @param channel The channel.
   * @param correlation The correlation.
   * @param netID The network identifier.
   * @param element The local element to sync.
   * @param listener The listener.
   */
  private void sync( Channel channel, int correlation, long netID, IModelObject element, UpdateListener listener)
  {
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
    public SyncRunnable( Channel channel, int correlation, long netID, IModelObject element, UpdateListener listener)
    {
      this.channel = channel;
      this.correlation = correlation;
      this.netID = netID;
      this.element = element;
      this.listener = listener;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
      sync( channel, correlation, netID, element, listener);
    }
    
    private Channel channel;
    private int correlation;
    private long netID;
    private IModelObject element;
    private UpdateListener listener;
  }
  
  private final static Log log = Log.getLog( SyncResponseProtocol.class);

  private BindProtocol bundle;
}
