package org.xmodel.net.transport.amqp;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.net.IXioChannel;
import org.xmodel.net.IXioPeerRegistry;
import org.xmodel.net.XioPeer;
import org.xmodel.net.execution.ExecutionPrivilege;
import org.xmodel.xpath.expression.IContext;

public class AmqpXioPeer extends XioPeer
{
  public AmqpXioPeer( 
      IXioChannel channel, 
      IXioPeerRegistry registry, 
      IContext context, 
      Executor executor, 
      ScheduledExecutorService scheduler,
      ExecutionPrivilege privilege)
  {
    super( channel, registry, context, executor, scheduler, privilege);
  }
}
