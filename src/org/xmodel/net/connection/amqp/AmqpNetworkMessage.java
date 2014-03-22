package org.xmodel.net.connection.amqp;

import org.xmodel.net.connection.INetworkMessage;

import com.rabbitmq.client.AMQP.BasicProperties;

public class AmqpNetworkMessage implements INetworkMessage
{
  public AmqpNetworkMessage( BasicProperties properties, byte[] body)
  {
    this.replyTo = properties.getReplyTo();
    this.correlation = properties.getCorrelationId();
    this.body = body;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkMessage#getBytes()
   */
  @Override
  public byte[] getBytes()
  {
    return body;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkMessage#getCorrelation()
   */
  @Override
  public Object getCorrelation()
  {
    return correlation;
  }
  
  /**
   * @return Returns the reply-to queue;
   */
  public String getReplyTo()
  {
    return replyTo;
  }
  
  /**
   * @return Returns the BasicProperties object for this message.
   */
  public BasicProperties getBasicProperties()
  {
    if ( replyTo == null && correlation == null) return null;
    
    BasicProperties.Builder builder = new BasicProperties.Builder();
    if ( replyTo != null) builder.replyTo( replyTo);
    if ( correlation != null) builder.correlationId( correlation.toString());
    return builder.build();
  }
  
  private String replyTo;
  private Object correlation;
  private byte[] body;
}