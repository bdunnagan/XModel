package org.xmodel.net.register;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.xmodel.log.Log;
import org.xmodel.net.XioChannelHandler.Type;

public class RegisterRequestProtocol
{
  public RegisterRequestProtocol( RegisterProtocol bundle)
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
   * Send an asynchronous registration request via the specified channel.
   * @param channel The channel.
   * @param name The name to associate with this peer.
   */
  public void send( Channel channel, String name) throws IOException, InterruptedException
  {
    log.debugf( "RegisterRequestProtocol.send: name=%s", name);
    
    byte[] bytes = name.getBytes();
    
    ChannelBuffer buffer = bundle.headerProtocol.writeHeader( 1 + bytes.length, Type.register, 0);
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
    log.debugf( "RegisterRequestProtocol.handle: name=%s", name);
    
    InetSocketAddress address = (InetSocketAddress)channel.getRemoteAddress();
    bundle.registry.register( name, address.getHostName());
  }
  
  private final static Log log = Log.getLog( RegisterRequestProtocol.class);

  private RegisterProtocol bundle;
}
