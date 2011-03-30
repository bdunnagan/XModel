package org.xmodel.log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * An implementation of Log.ISink that logs to a syslog host.
 */
public final class SyslogSink implements ILogSink
{
  public SyslogSink() throws SocketException
  {
    this( "127.0.0.1");
  }
  
  public SyslogSink( String host) throws SocketException
  {
    socket = new DatagramSocket();
    address = new InetSocketAddress( host, 514);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String)
   */
  @Override
  public void log( Log log, int level, String message)
  {
    send( String.format( "<%d>%s", getPriority( level), message));
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.Throwable)
   */
  @Override
  public void log( Log log, int level, Throwable throwable)
  {
    send( String.format( "<%d>%s", getPriority( level), throwable.toString()));
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String, java.lang.Throwable)
   */
  @Override
  public void log( Log log, int level, String message, Throwable throwable)
  {
    int priority = getPriority( level);
    send( String.format( "<%d>%s", priority, message));
    send( String.format( "<%d>%s", priority, throwable.toString()));
  }
  
  /**
   * Returns the syslog priority given the log level.
   * @param level The log level.
   * @return Returns the syslog priority given the log level.
   */
  private final int getPriority( int level)
  {
    int priority = 0;
    switch( level)
    {
      case Log.exception: priority = 1+8; break;
      case Log.verbose:   priority = 7+8; break;
      case Log.debug:     priority = 7+8; break;
      case Log.info:      priority = 6+8; break;
      case Log.warn:      priority = 4+8; break;
      case Log.error:     priority = 3+8; break;
      case Log.severe:    priority = 2+8; break;
      case Log.fatal:     priority = 0+8; break;
    }
    return priority;
  }
  
  /**
   * Send a syslog message.
   * @param message The message.
   */
  private final void send( String message)
  {
    try
    {
      byte[] bytes = message.getBytes();
      DatagramPacket packet = new DatagramPacket( bytes, bytes.length, address);
      socket.send( packet);
    }
    catch( IOException e)
    {
      System.err.println( message);
    }
  }
  
  private SocketAddress address;
  private DatagramSocket socket;
}
