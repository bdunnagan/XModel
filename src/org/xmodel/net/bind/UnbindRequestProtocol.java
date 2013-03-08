package org.xmodel.net.bind;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;
import org.xmodel.net.XioChannelHandler.Type;
import org.xmodel.net.XioException;

public class UnbindRequestProtocol
{
  public UnbindRequestProtocol( BindProtocol bundle)
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
   * Send an unbind request.
   * @param channel The channel.
   * @param netID The network identifier.
   * @return Returns null or the query result.
   */
  public void send( Channel channel, int netID) throws InterruptedException
  {
    log.debugf( "UnbindRequestProtocol.send: element=%X", netID);
    
    ChannelBuffer buffer = bundle.headerProtocol.writeHeader( 4, Type.unbindRequest, 0);
    buffer.writeInt( netID);
    
    // release resources
    bundle.requestCompressor.freeRemote( bundle.requestCompressor.findRemote( netID));
    
    // ignoring write buffer overflow for this type of messaging
    channel.write( buffer);
  }
  
  /**
   * Handle a bind request.
   * @param channel The channel.
   * @param buffer The buffer.
   */
  public void handle( Channel channel, ChannelBuffer buffer) throws XioException
  {
    int netID = buffer.readInt();
    
    IModelObject element = bundle.responseCompressor.findLocal( netID);
    if ( element == null) throw new XioException( String.format( "Element %X not found", netID));
    
    log.debugf( "UnbindRequestProtocol.handle: element=%X", netID);
    
    bundle.executor.execute( new UnbindRunnable( channel, netID, element));
  }
  
  /**
   * Unbind the element with the specified network identifier.
   * @param channel The channel.
   * @param netID The network identifier.
   * @param element The element.
   */
  private void unbind( Channel channel, long netID, IModelObject element)
  {
    synchronized( bundle.context)
    {
      try
      {
        // release resources
        bundle.responseCompressor.freeLocal( element);
      
        UpdateListener listener = bundle.bindRequestProtocol.getListener( element);
        if ( listener != null) listener.uninstall( element);
      }
      catch( Exception e)
      {
        SLog.exception( this, e);
      }
    }
  }
  
  private class UnbindRunnable implements Runnable
  {
    public UnbindRunnable( Channel channel, long netID, IModelObject element)
    {
      this.channel = channel;
      this.netID = netID;
      this.element = element;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
      unbind( channel, netID, element);
    }
    
    private Channel channel;
    private long netID;
    private IModelObject element;
  }
  
  private final static Log log = Log.getLog( UnbindRequestProtocol.class);

  private BindProtocol bundle;
}
