package org.xmodel.log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.xmodel.log.Log.ISink;

/**
 * An implementation of Log.ISink that logs to a syslog host.
 */
public final class SyslogSink implements ISink
{
  public SyslogSink() throws SocketException
  {
    this( "127.0.0.1");
  }
  
  public SyslogSink( String host) throws SocketException
  {
    calendar = Calendar.getInstance();
    dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS");
    dateFormat.setCalendar( calendar);
    socket = new DatagramSocket();
    address = new InetSocketAddress( host, 514);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(int, java.lang.String)
   */
  @Override
  public void log( int level, String message)
  {
    send( String.format( "<%d>%s %s\n", getPriority( level), dateFormat.format( new Date()), message));
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(int, java.lang.Throwable)
   */
  @Override
  public void log( int level, Throwable throwable)
  {
    log( level, "", throwable);
  }

  /* (non-Javadoc)
   * @see org.xmodel.log.Log.ISink#log(int, java.lang.String, java.lang.Throwable)
   */
  @Override
  public void log( int level, String message, Throwable throwable)
  {
    String date = dateFormat.format( new Date());
    int priority = getPriority( level);
    
    send( String.format( "<%d>%s %s\n", priority, date, message));
    
    StackTraceElement[] stack = throwable.getStackTrace();
    for( StackTraceElement element: stack)
    {
      send( String.format( "<%d>%s\n", priority, date, element.toString()));
    }
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
  
  private Calendar calendar;
  private SimpleDateFormat dateFormat;
  private SocketAddress address;
  private DatagramSocket socket;
}
