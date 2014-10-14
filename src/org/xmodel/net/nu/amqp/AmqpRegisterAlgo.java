package org.xmodel.net.nu.amqp;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.IRouter;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.algo.RegisterAlgo;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol;

public class AmqpRegisterAlgo extends RegisterAlgo
{
  public AmqpRegisterAlgo( IRouter router)
  {
    super( router);
  }
  
  @Override
  protected void handleRegister( ITransportImpl transport, IModelObject envelope)
  {
    IEnvelopeProtocol envelopeProtocol = transport.getProtocol().envelope();
    String replyQueue = envelopeProtocol.getReplyTo( envelope);
    AmqpNamedTransport childTransport = new AmqpNamedTransport( replyQueue, (AmqpTransport)transport);
    super.handleRegister( childTransport, envelope);
  }

  @Override
  protected void handleDeregister( ITransportImpl transport, IModelObject envelope)
  {
    IEnvelopeProtocol envelopeProtocol = transport.getProtocol().envelope();
    String replyQueue = envelopeProtocol.getReplyTo( envelope);
    AmqpNamedTransport childTransport = new AmqpNamedTransport( replyQueue, (AmqpTransport)transport);
    super.handleDeregister( childTransport, envelope);
  }
}
