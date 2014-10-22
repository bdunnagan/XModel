package org.xmodel.net.nu.amqp;

import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import org.xmodel.IModelObject;
import org.xmodel.net.nu.IRouter;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.algo.HeartbeatAlgo;
import org.xmodel.net.nu.algo.RegisterAlgo;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol;
import org.xmodel.net.nu.xaction.EventHandlerAdapter;

public class AmqpRegisterAlgo extends RegisterAlgo
{
  public AmqpRegisterAlgo( IRouter router, int heartbeatPeriod, int heartbeatTimeout, ScheduledExecutorService scheduler)
  {
    super( router);
    
    this.heartbeatPeriod = heartbeatPeriod;
    this.heartbeatTimeout = heartbeatTimeout;
    this.scheduler = scheduler;
  }
  
  @Override
  protected void handleRegister( ITransportImpl transport, IModelObject envelope)
  {
    IEnvelopeProtocol envelopeProtocol = transport.getProtocol().envelope();
    String replyQueue = envelopeProtocol.getReplyTo( envelope);
    
    AmqpNamedTransport childTransport = new AmqpNamedTransport( replyQueue, (AmqpTransport)transport);
    childTransport.getEventPipe().addFirst( childTransport);
    childTransport.getEventPipe().addFirst( new HeartbeatAlgo( childTransport, heartbeatPeriod, heartbeatTimeout, scheduler));
    childTransport.getEventPipe().addLast( transport.getEventPipe().getHandler( EventHandlerAdapter.class));
    childTransport.connect();
    
    super.handleRegister( childTransport, envelope);
  }

  @Override
  protected void handleDeregister( ITransportImpl transport, IModelObject envelope)
  {
    String name = transport.getProtocol().envelope().getRegistrationName( envelope);
    Iterator<ITransport> iter = ((AmqpTransport)transport).resolve( name);
    if ( iter.hasNext())
    {
      AmqpNamedTransport childTransport = (AmqpNamedTransport)iter.next();
      super.handleDeregister( childTransport, envelope);
      childTransport.disconnect( false);
    }
  }
  
  private int heartbeatPeriod;
  private int heartbeatTimeout;
  private ScheduledExecutorService scheduler;
}
