package org.xmodel.net.bind;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.HeaderProtocol.Type;
import org.xmodel.net.IXioChannel;
import org.xmodel.net.XioException;

public class SyncRequestProtocol
{
  public SyncRequestProtocol( BindProtocol bundle)
  {
    this.bundle = bundle;
  }
  
  /**
   * Reset this instance by releasing internal resources.  This method should be called after 
   * the channel is closed to prevent conflict between protocol traffic and the freeing of resources.
   */
  public void reset()
  {
  }
  
  /**
   * Send a sync request.
   * @param channel The channel.
   * @param key The network identifier.
   * @param timeout The timeout in milliseconds.
   * @return Returns null or the result of the sync.
   */
  public IModelObject send( IXioChannel channel, int netID, int timeout) throws InterruptedException
  {
    int correlation = bundle.syncResponseProtocol.nextCorrelation();
    log.debugf( "SyncRequestProtocol.send (sync): corr=%d, timeout=%d, netID=%X", correlation, timeout, netID);

    ChannelBuffer buffer = bundle.headerProtocol.writeHeader( 8, Type.syncRequest, 0);
    buffer.writeInt( correlation);
    buffer.writeInt( netID);
    
    // ignoring write buffer overflow for this type of messaging
    channel.write( buffer);
    
    return bundle.syncResponseProtocol.waitForResponse( correlation, timeout);
  }

  /**
   * Handle a sync request.
   * @param channel The channel.
   * @param buffer The buffer.
   */
  public void handle( IXioChannel channel, ChannelBuffer buffer) throws XioException
  {
    int correlation = buffer.readInt();
    int netID = buffer.readInt();
    
    IModelObject element = bundle.responseCompressor.findLocal( netID);
    if ( element == null) throw new XioException( String.format( "Element %X not found", netID));
    
    UpdateListener listener = bundle.bindRequestProtocol.getListener( element);
    if ( listener == null) throw new XioException( String.format( "Listener not found on %X", netID));
    
    bundle.executor.execute( new SyncRunnable( channel, correlation, netID, element, listener));
  }
  
  /**
   * Serve the sync request.
   * @param channel The channel.
   * @param correlation The correlation.
   * @param netID The network identifier.
   * @param element The local element to sync.
   * @param listener The listener.
   */
  private void sync( IXioChannel channel, int correlation, long netID, IModelObject element, UpdateListener listener)
  {
    synchronized( bundle.context)
    {
      try
      {
        // disable updates
        listener.setEnabled( false);
        
        // sync
        element.getChildren();
        
        // send response
        bundle.syncResponseProtocol.send( channel, correlation, element);
      }
      catch( IOException e)
      {
        log.exceptionf( e, "Failed to send sync response for %X", netID);
      }
      finally
      {
        listener.setEnabled( true);
      }
    }
  }
  
  private class SyncRunnable implements Runnable
  {
    public SyncRunnable( IXioChannel channel, int correlation, long netID, IModelObject element, UpdateListener listener)
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
    
    private IXioChannel channel;
    private int correlation;
    private long netID;
    private IModelObject element;
    private UpdateListener listener;
  }
  
  private final static Log log = Log.getLog( SyncResponseProtocol.class);

  private BindProtocol bundle;
}
