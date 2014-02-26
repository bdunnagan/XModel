package org.xmodel.net.echo;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.xmodel.net.IXioChannel;
import org.xmodel.net.XioChannelHandler.Type;

public class EchoRequestProtocol
{
  public EchoRequestProtocol( EchoProtocol bundle)
  {
    this.bundle = bundle;
  }
  
  /**
   * Send an execution request via the specified channel.
   * @param channel The channel.
   * @param context The local context.
   * @param vars Shared variables from the local context.
   * @param element The script element to execute.
   * @param timeout The timeout in milliseconds.
   * @return Returns the result.
   */
  public void send( IXioChannel channel) throws IOException
  {
    ChannelBuffer buffer = bundle.headerProtocol.writeHeader( 0, Type.echoRequest, 0);
    channel.write( buffer);
    sentOn = System.currentTimeMillis();
  }
  
  /**
   * Handle a request.
   * @param channel The channel.
   * @param buffer The buffer.
   */
  public void handle( IXioChannel channel, ChannelBuffer buffer) throws IOException
  {
    bundle.responseProtocol.send( channel);
  }
  
  private EchoProtocol bundle;
  protected long sentOn;
}
