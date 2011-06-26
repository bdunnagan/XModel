package org.xmodel.net.stream;

import java.io.IOException;

public class TcpClient extends TcpManager
{
  public TcpClient() throws IOException
  {
    super();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.stream.TcpManager#connect(java.lang.String, int, int, org.xmodel.net.stream.ITcpListener)
   */
  @Override
  public Connection connect( String host, int port, int timeout, ITcpListener listener) throws IOException
  {
    return super.connect( host, port, timeout, listener);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.stream.TcpManager#reconnect(org.xmodel.net.stream.Connection, int)
   */
  @Override
  public boolean reconnect( Connection connection, int timeout) throws IOException
  {
    return super.reconnect( connection, timeout);
  }
}
