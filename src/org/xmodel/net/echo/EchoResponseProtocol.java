package org.xmodel.net.echo;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.xmodel.log.Log;
import org.xmodel.net.HeaderProtocol.Type;
import org.xmodel.net.IXioChannel;

public class EchoResponseProtocol
{
  public EchoResponseProtocol( EchoProtocol bundle)
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
   * Send an echo response.
   * @param channel The channel.
   */
  public void send( IXioChannel channel) throws IOException
  {
    ChannelBuffer buffer = bundle.headerProtocol.writeHeader( 0, Type.echoResponse, 0);
    channel.write( buffer);
  }
  
  /**
   * Handle a response.
   * @param channel The channel.
   * @param buffer The buffer.
   */
  public void handle( IXioChannel channel, ChannelBuffer buffer) throws IOException
  {
    long latency = System.currentTimeMillis() - bundle.requestProtocol.sentOn;
    log.debugf( "Latency %dms to %s", latency, channel.getRemoteAddress());
  }
  
  private final static Log log = Log.getLog( EchoResponseProtocol.class);
  
  private EchoProtocol bundle;
}
