package org.xmodel.net.stream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;;

/**
 * Convenience class for handling SSL.
 */
public class SSL
{
  /**
   * Read data from specified channel and handle SSL handshaking.
   * @param channel The channel.
   * @param connection The connection that is being read.
   * @return Returns the number of bytes read;
   */
  public int read( SocketChannel channel, Connection connection) throws IOException
  {
    int nread = channel.read( buffer);
    if ( nread == -1)
    {
      engine.closeInbound();
      return -1;
    }

    int start = connection.buffer.position();
    
    SSLEngineResult sslResult = engine.unwrap( buffer, connection.buffer);
    runDelegatedTasks( sslResult);

    buffer.compact();
    
    switch( sslResult.getStatus()) 
    {
      case BUFFER_OVERFLOW:
      {
        int required = engine.getSession().getApplicationBufferSize();
        ByteBuffer larger = ByteBuffer.allocate( required + connection.buffer.position());
        connection.buffer.flip();
        larger.put( connection.buffer);
        connection.buffer = larger;
        return read( channel, connection);
      }
      
      case BUFFER_UNDERFLOW:
      {
        int required = engine.getSession().getPacketBufferSize();
        if ( required > buffer.capacity()) 
        {
          ByteBuffer larger = ByteBuffer.allocate( required);
          buffer.flip();
          larger.put( buffer);
          buffer = larger;
        }
        return 0;
      }
      
      case CLOSED:
      {
        return -1;
      }
      
      default:
      {
        return buffer.position() - start;
      }
    }
  }
  
  /**
   * Write data to the specified channel and handle SSL handshaking.
   * @param channel The channel.
   * @param data The data to be sent.
   */
  public void write( SocketChannel channel, ByteBuffer data) throws IOException
  {
    SSLEngineResult sslResult = engine.wrap( data, buffer);
    runDelegatedTasks( sslResult);
    
    switch( sslResult.getStatus()) 
    {
      case BUFFER_OVERFLOW:
      {
        int required = engine.getSession().getPacketBufferSize();
        if ( required > buffer.capacity()) 
        {
          ByteBuffer larger = ByteBuffer.allocate( required);
          buffer.flip();
          larger.put( buffer);
          buffer = larger;
        }
        
        write( channel, data);
        return;
      }
      
      case CLOSED:
      {
        return;
      }
    }
  }
  
  /**
   * Handle SSL tasks.
   * @param result The SSLEngine result from the last read.
   */
  private void runDelegatedTasks( SSLEngineResult result)
  {
    if ( result.getHandshakeStatus() == HandshakeStatus.NEED_TASK)
    {
      Runnable runnable;
      while ( (runnable = engine.getDelegatedTask()) != null)
      {
        runnable.run();
      }
    }
  }

  public boolean isClosed()
  {
    return (engine.isOutboundDone() && engine.isInboundDone());
  }

  private SSLEngine engine;
  private ByteBuffer buffer;
}
