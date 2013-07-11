package org.xmodel.net.echo;

import java.io.IOException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.xmodel.log.Log;
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
  public void send( Channel channel) throws IOException
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
  public void handle( Channel channel, ChannelBuffer buffer) throws IOException
  {
    RequestRunnable runnable = new RequestRunnable( channel);
    bundle.executor.execute( runnable);
  }
  
  private class RequestRunnable implements Runnable
  {
    public RequestRunnable( Channel channel)
    {
      this.channel = channel;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
      try
      {
        bundle.responseProtocol.send( channel);
      }
      catch( IOException e)
      {
        log.warnf( "Unable to send echo response to %s", channel.getRemoteAddress());
      }
    }

    private Channel channel;
  }
  
  private final static Log log = Log.getLog( EchoRequestProtocol.class);

  private EchoProtocol bundle;
  protected long sentOn;
}
