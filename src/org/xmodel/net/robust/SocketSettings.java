/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.net.robust;

import java.net.Socket;
import java.net.SocketException;

/**
 * A class which holds the socket settings for a socket-based ISession.
 */
public class SocketSettings
{
  public SocketSettings()
  {
    keepAlive = true;
    noDelay = true;
    trafficClass = 0x10; // low-delay
    connectionTimePriority = 2;
    latencyPriority = 1;
    bandwidthPriority = 0;
  }
  
  /**
   * Set the send buffer size.
   * @param size The buffer size.
   */
  public void setSendBufferSize( int size)
  {
    sendBufferSize = size;
  }
  
  /**
   * Set the receive buffer size.
   * @param size The buffer size.
   */
  public void setReceiveBufferSize( int size)
  {
    receiveBufferSize = size;
  }
  
  /**
   * Enable keepalive on the socket.
   * @param enable True if keepalive should be enabled.
   */
  public void setKeepAlive( boolean enable)
  {
    keepAlive = enable;
  }

  /**
   * Disable/enable Nagel's algorithm.
   * @param noDelay True if Nagel's algorithm should be disabled.
   */
  public void setTcpNoDelay( boolean noDelay)
  {
    this.noDelay = noDelay;
  }
  
  /**
   * Set the traffic class for the underlying socket.
   * @param trafficClass The traffic class as defined for the <code>java.net.Socket</code> class.
   */
  public void setTrafficClass( int trafficClass)
  {
    this.trafficClass = trafficClass;
  }
  
  /**
   * Set the performance preferences for the underlying socket.
   * @param connectionTime 
   * @param latency
   * @param bandwidth
   */
  public void setPerformancePreferences( int connectionTime, int latency, int bandwidth)
  {
    connectionTimePriority = connectionTime;
    latencyPriority = latency;
    bandwidthPriority = bandwidth;
  }
  
  /**
   * Configure the specified socket with these settings.
   * @param socket The socket.
   */
  protected void configure( Socket socket) throws SocketException
  {
    if ( sendBufferSize > 0) socket.setSendBufferSize( sendBufferSize);
    if ( receiveBufferSize > 0) socket.setReceiveBufferSize( receiveBufferSize);
    socket.setKeepAlive( keepAlive);
    socket.setTcpNoDelay( noDelay);
    socket.setTrafficClass( trafficClass);
    socket.setPerformancePreferences( connectionTimePriority, latencyPriority, bandwidthPriority);
  }
  
  public int trafficClass;
  public boolean keepAlive;
  public boolean noDelay;
  public int sendBufferSize;
  public int receiveBufferSize;
  public int connectionTimePriority;
  public int latencyPriority;
  public int bandwidthPriority;
}
