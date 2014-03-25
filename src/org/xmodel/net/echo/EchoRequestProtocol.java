package org.xmodel.net.echo;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.xmodel.net.HeaderProtocol.Type;
import org.xmodel.net.XioChannel;

public class EchoRequestProtocol
{
  public EchoRequestProtocol( EchoProtocol bundle)
  {
    this.bundle = bundle;
  }
  
  /**
   * Send an execution request via the specified channel.
   * @param channel The channel.
   * @return Returns the result.
   */
  public void send( XioChannel channel) throws IOException
  {
    ChannelBuffer buffer = bundle.headerProtocol.writeHeader( 0, Type.echoRequest, 0);
    channel.send( buffer);
    sentOn = System.currentTimeMillis();
  }
  
  /**
   * Handle a request.
   * @param channel The channel.
   * @param buffer The buffer.
   */
  public void handle( XioChannel channel, ChannelBuffer buffer) throws IOException
  {
    bundle.responseProtocol.send( channel);
  }
  
  private EchoProtocol bundle;
  protected long sentOn;
}
