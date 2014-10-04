package org.xmodel.net.nu.protocol;

import org.xmodel.IModelObject;

/**
 * (thread-safe)
 */
public interface IEnvelopeProtocol
{
  public enum Type { request, response, ack, register, deregister};
  
  public IModelObject buildRegisterEnvelope( String key, String name);
  
  public IModelObject buildDeregisterEnvelope( String key, String name);
  
  public IModelObject buildRequestEnvelope( String key, String route, IModelObject message);
  
  public IModelObject buildResponseEnvelope( String key, String route, IModelObject message);
  
  public IModelObject buildAck( String key, String route);
  
  public Type getType( IModelObject envelope);
    
  public String getRegistrationName( IModelObject envelope);
  
  public IModelObject getMessage( IModelObject envelope);
  
  public IModelObject getEnvelope( IModelObject message);
  
  public String getKey( IModelObject envelope);
  
  public String getRoute( IModelObject envelope);

  public void setReplyTo( IModelObject envelope, String replyTo);
  
  public String getReplyTo( IModelObject envelope);
}
