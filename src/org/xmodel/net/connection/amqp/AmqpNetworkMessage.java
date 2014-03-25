package org.xmodel.net.connection.amqp;

import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.xmodel.net.connection.INetworkMessage;

import com.rabbitmq.client.AMQP.BasicProperties;

public class AmqpNetworkMessage implements INetworkMessage
{
  public AmqpNetworkMessage( BasicProperties properties, byte[] body)
  {
    this.replyTo = properties.getReplyTo();
    this.correlation = properties.getCorrelationId();
    this.body = body;
    this.responseRef = new AtomicReference<INetworkMessage>();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkMessage#getCorrelation()
   */
  public Object getCorrelation()
  {
    // TODO: Remove this after verification
    if ( correlation != null && correlation.toString().length() == 0) throw new IllegalStateException();
    return correlation;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkMessage#getExpiration()
   */
  @Override
  public long getExpiration()
  {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkMessage#setResponse(org.xmodel.net.connection.INetworkMessage)
   */
  @Override
  public void setResponse( INetworkMessage message)
  {
    responseRef.set( message);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkMessage#getResponse()
   */
  @Override
  public INetworkMessage getResponse()
  {
    return responseRef.get();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkMessage#getBytes()
   */
  public byte[] getBytes()
  {
    return body;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.connection.INetworkMessage#getChannelBuffer()
   */
  @Override
  public ChannelBuffer getChannelBuffer()
  {
    return ChannelBuffers.wrappedBuffer( getBytes());
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
  private AtomicReference<INetworkMessage> responseRef;
}