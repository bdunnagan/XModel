package org.xmodel.net.nu.protocol;

import org.xmodel.IModelObject;

/**
 * (thread-safe)
 */
public interface IEnvelopeProtocol
{
  public IModelObject buildRequestEnvelope( String key, String route, IModelObject message);
  
  public IModelObject buildResponseEnvelope( String key, String route, IModelObject message);
  
  public IModelObject buildAck( String key, String route);
  
  public boolean isRequest( IModelObject envelope);
  
  public IModelObject getMessage( IModelObject envelope);
  
  public IModelObject getEnvelope( IModelObject message);
  
  public String getKey( IModelObject envelope);
  
  public String getRoute( IModelObject envelope);
  
  public boolean isAck( IModelObject envelope);
}
