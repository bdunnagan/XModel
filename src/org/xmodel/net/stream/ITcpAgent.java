package org.xmodel.net.stream;

import java.io.IOException;

/**
 * A common interface for TCP clients and servers used by the Connection class.
 */
public interface ITcpAgent
{
  /**
   * Process next event from the socket and wait forever.
   */
  public void process() throws IOException;
  
  /**
   * Process next event from the socket.
   * @param timeout The amount of time to wait.
   */
  public void process( int timeout) throws IOException;
}
