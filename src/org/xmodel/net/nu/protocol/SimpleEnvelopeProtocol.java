package org.xmodel.net.nu.protocol;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;

public final class SimpleEnvelopeProtocol implements IEnvelopeProtocol
{
  @Override
  public IModelObject buildHeartbeatEnvelope()
  {
    return new ModelObject( "h");
  }
  
  @Override
  public IModelObject buildRegisterEnvelope( String name, int life)
  {
    IModelObject envelope = new ModelObject( "r");
    if ( life > 0) envelope.setAttribute( expiryAttrName, System.currentTimeMillis() + life);
    envelope.setAttribute( "name", name);
    return envelope;
  }

  @Override
  public IModelObject buildDeregisterEnvelope( String name, int life)
  {
    IModelObject envelope = new ModelObject( "d");
    if ( life > 0) envelope.setAttribute( expiryAttrName, System.currentTimeMillis() + life);
    envelope.setAttribute( "name", name);
    return envelope;
  }

  @Override
  public IModelObject buildRequestEnvelope( String route, IModelObject message, int life)
  {
    IModelObject envelope = new ModelObject( "q");
    if ( life > 0) envelope.setAttribute( expiryAttrName, System.currentTimeMillis() + life);
    if ( route != null) envelope.setAttribute(  routeAttrName, route);
    envelope.addChild( message);
    return envelope;
  }

  @SuppressWarnings("unchecked")
  @Override
  public IModelObject buildResponseEnvelope( IModelObject requestEnvelope, Object message)
  {
    IModelObject envelope = new ModelObject( "s");
    envelope.setAttribute( keyAttrName, requestEnvelope.getAttribute( keyAttrName));
    envelope.setAttribute( routeAttrName, requestEnvelope.getAttributeNode( routeAttrName));
    envelope.setAttribute( channelAttrName, requestEnvelope.getAttributeNode( channelAttrName));
    
    if ( message instanceof List)
    {
      for( Object element: (List<Object>)message)
        envelope.addChild( (IModelObject)element);
    }
    else if ( message instanceof IModelObject)
    {
      envelope.addChild( (IModelObject)message);
    }
    else
    {
      envelope.setAttribute( valueMessageAttrName, message);
    }
    
    return envelope;
  }

  @Override
  public IModelObject buildAck( IModelObject requestEnvelope)
  {
    IModelObject ack = new ModelObject( "a");
    ack.setAttribute( keyAttrName, requestEnvelope.getAttribute( keyAttrName));
    ack.setAttribute( routeAttrName, requestEnvelope.getAttributeNode( routeAttrName));
    ack.setAttribute( channelAttrName, requestEnvelope.getAttributeNode( channelAttrName));
    return ack;
  }

  @Override
  public Type getType( IModelObject envelope)
  {
    switch( envelope.getType().charAt( 0))
    {
      case 'r': return Type.register;
      case 'd': return Type.deregister;
      case 'q': return Type.request;
      case 's': return Type.response;
      case 'a': return Type.ack;
      case 'h': return Type.heartbeat;
      default:  return null;
    }
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
  public Object getMessage( IModelObject envelope)
  {
    if ( envelope == null) return null;
    
    Object message = envelope.getAttribute( valueMessageAttrName);
    if ( message != null) return message;
    
    return envelope.getChildren();
  }

  @Override
  public IModelObject getEnvelope( IModelObject message)
  {
    if ( message == null) return null;
    return message.getParent();
  }

  @Override
  public long getExpiration( IModelObject envelope)
  {
    return Xlate.get( envelope, expiryAttrName, -1L);
  }

  @Override
  public void setChannel( IModelObject envelope, Object channel)
  {
    envelope.setAttribute( channelAttrName, channel);
  }

  @Override
  public Object getChannel( IModelObject envelope)
  {
    return envelope.getAttribute( channelAttrName);
  }

  @Override
  public void setKey( IModelObject envelope, Object key)
  {
    if ( key != null) envelope.setAttribute( keyAttrName, key);
  }

  @Override
  public Object getKey( IModelObject envelope)
  {
    return envelope.getAttribute( keyAttrName);
  }

  @Override
  public String getRoute( IModelObject envelope)
  {
    return Xlate.get( envelope, routeAttrName, (String)null);
  }

  @Override
  public void setReplyTo( IModelObject envelope, String replyTo)
  {
    Xlate.set( envelope, replyAttrName, replyTo);
  }

  @Override
  public String getReplyTo( IModelObject envelope)
  {
    return Xlate.get( envelope, replyAttrName, (String)null);
  }
  
  private final static String keyAttrName = "k";
  private final static String routeAttrName = "u";
  private final static String expiryAttrName = "e";
  private final static String replyAttrName = "r";
  private final static String channelAttrName = "c";
  private final static String valueMessageAttrName = "v";
}
