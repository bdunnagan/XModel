package org.xmodel.net.nu.protocol;

import org.xmodel.IModelObject;

/**
 * (thread-safe)
 */
public interface IEnvelopeProtocol
{
  public enum Type { heartbeat, request, response, ack, register, deregister};
  
  public IModelObject buildHeartbeatEnvelope();

  public IModelObject buildRegisterEnvelope( String name, int life);
  
  public IModelObject buildDeregisterEnvelope( String name, int life);
  
  public IModelObject buildRequestEnvelope( String route, IModelObject message, int life);
  
  public IModelObject buildResponseEnvelope( IModelObject requestEnvelope, IModelObject message);
  
  public IModelObject buildAck( IModelObject requestEnvelope);
  
  public Type getType( IModelObject envelope);
  
  public boolean isRequest( IModelObject envelope);
    
  public String getRegistrationName( IModelObject envelope);
  
  public IModelObject getMessage( IModelObject envelope);
  
  public IModelObject getEnvelope( IModelObject message);
  
  public long getExpiration( IModelObject envelope);
  
  public void setChannel( IModelObject envelope, Object channel);
  
  public Object getChannel( IModelObject envelope);
  
  public void setKey( IModelObject envelope, Object key);
  
  public Object getKey( IModelObject envelope);
  
  public String getRoute( IModelObject envelope);

  public void setReplyTo( IModelObject envelope, String replyTo);
  
  public String getReplyTo( IModelObject envelope);
}
