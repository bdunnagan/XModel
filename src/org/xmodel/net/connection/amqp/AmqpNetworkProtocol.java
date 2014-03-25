package org.xmodel.net.connection.amqp;

import org.xmodel.net.connection.INetworkProtocol;

public class AmqpNetworkProtocol implements INetworkProtocol
{
  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkProtocol#getCorrelation(java.lang.Object)
   */
  @Override
  public Object getCorrelation( Object message)
  {
    return ((AmqpNetworkMessage)message).getCorrelation();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkProtocol#getBytes(java.lang.Object)
   */
  @Override
  public byte[] getBytes( Object message)
  {
    return ((AmqpNetworkMessage)message).getBytes();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkProtocol#createEchoRequest()
   */
  @Override
  public Object createEchoRequest()
  {
    // TODO Auto-generated method stub
    return null;
  }
}
