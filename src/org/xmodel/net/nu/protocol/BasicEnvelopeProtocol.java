package org.xmodel.net.nu.protocol;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.net.nu.IEnvelopeProtocol;

public class BasicEnvelopeProtocol implements IEnvelopeProtocol
{
  @Override
  public IModelObject buildEnvelope( String key, String route, IModelObject message)
  {
    IModelObject envelope = new ModelObject( "envelope");
    if ( key != null) envelope.setAttribute( "key", key);
    if ( route != null) envelope.setAttribute(  "route", route);
    envelope.addChild( message);
    return envelope;
  }

  @Override
  public IModelObject getMessage( IModelObject envelope)
  {
    return envelope.removeChild( 0);
  }

  @Override
  public String getKey( IModelObject envelope)
  {
    return Xlate.get( envelope, "id", (String)null);
  }

  @Override
  public String getRoute( IModelObject envelope)
  {
    return Xlate.get( envelope, "route", (String)null);
  }
}
