package org.xmodel.net.register;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.xmodel.log.Log;
import org.xmodel.net.XioChannelHandler.Type;

public class UnregisterRequestProtocol
{
  public UnregisterRequestProtocol( RegisterProtocol bundle)
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
   * Send an asynchronous cancel request via the specified channel.
   * @param channel The channel.
   * @param name The name to associate with this peer.
   */
  public void send( Channel channel, String name) throws IOException, InterruptedException
  {
    log.debugf( "UnregisterRequestProtocol.send: name=%s", name);
    
    byte[] bytes = name.getBytes();
    
    ChannelBuffer buffer = bundle.headerProtocol.writeHeader( 1 + bytes.length, Type.unregister, 0);
    buffer.writeByte( bytes.length);
    buffer.writeBytes( bytes);
    
    // ignoring write buffer overflow for this type of messaging
    channel.write( buffer);
  }
  
  /**
   * Handle a request.
   * @param channel The channel.
   * @param buffer The buffer.
   */
  public void handle( Channel channel, ChannelBuffer buffer) throws IOException
  {
    int length = buffer.readByte();
    if ( length < 0) length += 256;
    
    byte[] bytes = new byte[ length];
    buffer.readBytes( bytes);
    
    String name = new String( bytes);
    log.debugf( "UnregisterRequestProtocol.handle: name=%s", name);
    
    InetSocketAddress address = (InetSocketAddress)channel.getRemoteAddress();
    bundle.registry.unregister( name, address.getAddress().getHostAddress());
  }
  
  private final static Log log = Log.getLog( UnregisterRequestProtocol.class);

  private RegisterProtocol bundle;
}
