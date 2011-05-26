package org.xmodel.net.nu.stream;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.xmodel.net.nu.INetSender;

/**
 * An implementation of INetSender for use with the Connection class.
 */
public class TcpNetSender implements INetSender, IReceiver
{
  public TcpNetSender( Connection connection)
  {
    this.connection = connection;
    connection.addReceiver( this);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.INetSender#close()
   */
  @Override
  public void close()
  {
    connection.removeReceiver( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.INetSender#send(java.nio.ByteBuffer)
   */
  @Override
  public boolean send( ByteBuffer buffer)
  {
    try
    {
      connection.write( buffer);
      return true;
    }
    catch( IOException e)
    {
      return false;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.INetSender#send(java.nio.ByteBuffer, int)
   */
  @Override
  public ByteBuffer send( ByteBuffer buffer, int timeout)
  {
    send( buffer);
    
    try
    {
      while( received == null && timeout > 0)
      {
        long t0 = System.nanoTime();
        connection.getAgent().process( timeout);
        long t1 = System.nanoTime();
        timeout -= (t1 - t0) * 1e-6;
      }
      
      ByteBuffer result = received;
      received = null;
      return result;
    }
    catch( IOException e)
    {
      return null;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.stream.IReceiver#received(org.xmodel.net.nu.stream.Connection, java.nio.ByteBuffer)
   */
  @Override
  public void received( Connection connection, ByteBuffer buffer)
  {
    received = buffer;
  }

  private Connection connection;
  private ByteBuffer received;
}
