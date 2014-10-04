package org.xmodel.net.nu.protocol;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;

public class SimpleEnvelopeProtocol implements IEnvelopeProtocol
{
  @Override
  public IModelObject buildRegisterEnvelope( String key, String name)
  {
    IModelObject envelope = new ModelObject( "register");
    envelope.setAttribute( "key", key);
    envelope.setAttribute( "name", name);
    return envelope;
  }

  @Override
  public IModelObject buildDeregisterEnvelope( String key, String name)
  {
    IModelObject envelope = new ModelObject( "deregister");
    envelope.setAttribute( "key", key);
    envelope.setAttribute( "name", name);
    return envelope;
  }

  @Override
  public IModelObject buildRequestEnvelope( String key, String route, IModelObject message)
  {
    IModelObject envelope = new ModelObject( "request");
    
    if ( key != null) envelope.setAttribute( "key", key);
    if ( route != null) envelope.setAttribute(  "route", route);
    
    envelope.addChild( message);
    return envelope;
  }

  @Override
  public IModelObject buildResponseEnvelope( String key, String route, IModelObject message)
  {
    IModelObject envelope = new ModelObject( "response");
    
    if ( key != null) envelope.setAttribute( "key", key);
    if ( route != null) envelope.setAttribute(  "route", route);
    
    envelope.addChild( message);
    return envelope;
  }

  @Override
  public IModelObject buildAck( String key, String route)
  {
    IModelObject ack = new ModelObject( "ack");
    
    if ( key != null) ack.setAttribute( "key", key);
    if ( route != null) ack.setAttribute(  "route", route);
    
    return ack;
  }

  @Override
  public Type getType( IModelObject envelope)
  {
    return Type.valueOf( envelope.getType());
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
    IModelObject envelope = message.getRoot();
    if ( envelope.isType( "request") || envelope.isType( "response")) return envelope;
    throw new IllegalArgumentException();
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
