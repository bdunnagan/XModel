package org.xmodel.net.connection.amqp;

import com.rabbitmq.client.AMQP.BasicProperties;

public class AmqpNetworkMessage
{
  public AmqpNetworkMessage( BasicProperties properties, byte[] body)
  {
    this.replyTo = properties.getReplyTo();
    this.correlation = properties.getCorrelationId();
    this.body = body;
  }

  /**
   * @return Returns the bytes of the message.
   */
  public byte[] getBytes()
  {
    return body;
  }

  /**
   * @return Returns null or the message correlation.
   */
  public Object getCorrelation()
  {
    // TODO: Remove this after verification
    if ( correlation != null && correlation.toString().length() == 0) throw new IllegalStateException();
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