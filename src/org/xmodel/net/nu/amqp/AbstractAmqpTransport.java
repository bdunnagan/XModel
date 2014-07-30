package org.xmodel.net.nu.amqp;

import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.AbstractTransport;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xpath.expression.IContext;

public class AbstractAmqpTransport extends AbstractTransport
{
  public AbstractAmqpTransport( Protocol protocol, IContext transportContext, ScheduledExecutorService scheduler)
  {
    super( protocol, transportContext, scheduler);
  }
  
  @Override
  public AsyncFuture<ITransport> sendImpl( IModelObject envelope)
  {
    return null;
  }

  @Override
  public AsyncFuture<ITransport> connect( int timeout)
  {
    
    
    return null;
  }

  @Override
  public AsyncFuture<ITransport> disconnect()
  {
    return null;
  }
}
