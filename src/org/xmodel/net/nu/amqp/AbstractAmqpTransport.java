package org.xmodel.net.nu.amqp;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.FailureAsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.net.nu.AbstractTransport;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class AbstractAmqpTransport extends AbstractTransport
{
  public AbstractAmqpTransport( Protocol protocol, IContext transportContext, ScheduledExecutorService scheduler)
  {
    super( protocol, transportContext, scheduler);
    
    connectionFactory = new ConnectionFactory();
    connectionFactory.setHost( "localhost");
  }
  
  public void setRemoteAddress( String host)
  {
    connectionFactory.setHost( host);
  }
  
  @Override
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope)
  {
    return null;
  }

  @Override
  public AsyncFuture<ITransport> connect( int timeout)
  {
    try
    {
      connection = connectionFactory.newConnection();
      sendChannel = connection.createChannel();
      return new SuccessAsyncFuture<ITransport>( this);
    }
    catch( IOException e)
    {
      return new FailureAsyncFuture<ITransport>( this, e);
    }
  }

  @Override
  public AsyncFuture<ITransport> disconnect()
  {
    return null;
  }
  
  private ConnectionFactory connectionFactory;
  private Connection connection;
  private Channel sendChannel;
}
