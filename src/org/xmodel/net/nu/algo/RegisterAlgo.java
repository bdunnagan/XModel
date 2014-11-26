package org.xmodel.net.nu.algo;

import java.io.IOException;
import java.util.Iterator;
import org.xmodel.IModelObject;
import org.xmodel.net.nu.DefaultEventHandler;
import org.xmodel.net.nu.EventPipe;
import org.xmodel.net.nu.IRouter;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol.Type;
import org.xmodel.xpath.expression.IContext;

public class RegisterAlgo extends DefaultEventHandler
{
  public RegisterAlgo( IRouter router)
  {
    this.router = router;
  }

  public IRouter getRouter()
  {
    return router;
  }
  
  @Override
  public boolean notifyDisconnect( ITransportImpl transport, IContext transportContext) throws IOException
  {
    // remove routes
    Iterator<String> routes = router.removeRoutes( transport);

    if ( routes != null)
    {
      // send deregister notification
      EventPipe eventPipe = transport.getEventPipe();
      while( routes.hasNext())
      {
        String route = routes.next();
        eventPipe.notifyDeregister( transport, transport.getTransportContext(), route);
      }
    }
    
    return false;
  }

  @Override
  public boolean notifyReceive( ITransportImpl transport, IModelObject envelope, IContext messageContext, IModelObject request)
  {
    if ( request == null)
    {
      Type type = transport.getProtocol().envelope().getType( envelope);
      switch( type)
      {
        case register:   handleRegister( transport, envelope); return true;
        case deregister: handleDeregister( transport, envelope); return true;
        default:         break;
      }
    }
    return false;
  }
  
  protected void handleRegister( ITransportImpl transport, IModelObject envelope)
  {
    IEnvelopeProtocol envelopeProtocol = transport.getProtocol().envelope();
    String route = envelopeProtocol.getRoute( envelope);
    if ( route == null)
    {
      String name = envelopeProtocol.getRegistrationName( envelope);
      router.addRoute( name, transport);
      transport.sendAck( envelope);
      transport.getEventPipe().notifyRegister( transport, transport.getTransportContext(), name);
    }
    else
    {
      // TODO
      throw new UnsupportedOperationException();
    }
  }
  
  protected void handleDeregister( ITransportImpl transport, IModelObject envelope)
  {
    IEnvelopeProtocol envelopeProtocol = transport.getProtocol().envelope();
    String route = envelopeProtocol.getRoute( envelope);
    if ( route == null)
    {
      String name = envelopeProtocol.getRegistrationName( envelope);
      router.removeRoute( name, transport);
      transport.sendAck( envelope);
      transport.getEventPipe().notifyDeregister( transport, transport.getTransportContext(), name);
    }
    else
    {
      // TODO
      throw new UnsupportedOperationException();
    }
  }
  
  private IRouter router;
}
