package org.xmodel.net.nu.protocol;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;

public class SimpleEnvelopeProtocol implements IEnvelopeProtocol
{
  @Override
  public IModelObject buildHeartbeatEnvelope()
  {
    return new ModelObject( "heartbeat");
  }
  
  @Override
  public IModelObject buildRegisterEnvelope( String name)
  {
    IModelObject envelope = new ModelObject( "register");
    envelope.setAttribute( "name", name);
    return envelope;
  }

  @Override
  public IModelObject buildDeregisterEnvelope( String name)
  {
    IModelObject envelope = new ModelObject( "deregister");
    envelope.setAttribute( "name", name);
    return envelope;
  }

  @Override
  public IModelObject buildRequestEnvelope( String route, IModelObject message)
  {
    IModelObject envelope = new ModelObject( "request");
    if ( route != null) envelope.setAttribute(  "route", route);
    envelope.addChild( message);
    return envelope;
  }

  @Override
  public IModelObject buildResponseEnvelope( IModelObject requestEnvelope, IModelObject message)
  {
    IModelObject envelope = new ModelObject( "response");
    envelope.setAttribute( "key", requestEnvelope.getAttribute( "key"));
    envelope.setAttribute( "route", requestEnvelope.getAttribute( "route"));
    envelope.addChild( message);
    return envelope;
  }

  @Override
  public IModelObject buildAck( IModelObject requestEnvelope)
  {
    IModelObject ack = new ModelObject( "ack");
    ack.setAttribute( "key", requestEnvelope.getAttribute( "key"));
    ack.setAttribute( "route", requestEnvelope.getAttribute( "route"));
    return ack;
  }

  @Override
  public Type getType( IModelObject envelope)
  {
    return Type.valueOf( envelope.getType());
  }

  @Override
  public boolean isRequest( IModelObject envelope)
  {
    switch( getType( envelope))
    {
      case request:
      case register:
      case deregister:
        return true;
        
      default:
        return false;
    }
  }

  @Override
  public String getRegistrationName( IModelObject envelope)
  {
    return Xlate.get( envelope, "name", (String)null);
  }

  @Override
  public IModelObject getMessage( IModelObject envelope)
  {
    return envelope.getChild( 0);
  }

  @Override
  public IModelObject getEnvelope( IModelObject message)
  {
    return message.getRoot();
  }

  @Override
  public void setKey( IModelObject envelope, String key)
  {
    if ( key != null) envelope.setAttribute( "key", key);
  }

  @Override
  public String getKey( IModelObject envelope)
  {
    return Xlate.get( envelope, "key", (String)null);
  }

  @Override
  public String getRoute( IModelObject envelope)
  {
    return Xlate.get( envelope, "route", (String)null);
  }

  @Override
  public void setReplyTo( IModelObject envelope, String replyTo)
  {
    Xlate.set( envelope, "reply", replyTo);
  }

  @Override
  public String getReplyTo( IModelObject envelope)
  {
    return Xlate.get( envelope, "reply", (String)null);
  }
}
