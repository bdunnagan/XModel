package org.xmodel.net.nu.protocol;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;

public class SimpleEnvelopeProtocol implements IEnvelopeProtocol
{
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
  public boolean isRequest( IModelObject envelope)
  {
    return envelope.isType( "request");
  }

  @Override
  public IModelObject getMessage( IModelObject envelope)
  {
    return envelope.getChild( 0);
  }

  @Override
  public IModelObject getEnvelope( IModelObject message)
  {
    IModelObject envelope = message.getParent();
    if ( envelope.isType( "envelope")) return envelope;
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
  public boolean isAck( IModelObject envelope)
  {
    return envelope.isType( "ack");
  }
}
