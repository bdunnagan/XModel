package org.xmodel.net.nu.protocol;

import org.xmodel.IModelObject;

/**
 * (thread-safe)
 */
public interface IEnvelopeProtocol
{
  public IModelObject buildEnvelope( String key, String route, IModelObject message);
  
  public IModelObject getMessage( IModelObject envelope);
  
  public String getKey( IModelObject envelope);
  
  public String getRoute( IModelObject envelope);
}
