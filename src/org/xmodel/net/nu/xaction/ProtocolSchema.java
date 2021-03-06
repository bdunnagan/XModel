package org.xmodel.net.nu.xaction;

import org.xmodel.net.nu.protocol.SimpleEnvelopeProtocol;
import org.xmodel.net.nu.protocol.IWireProtocol;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

final class ProtocolSchema
{
  public static Protocol getProtocol( IExpression protocolExpr, IContext context)
  {
    String protocol = (protocolExpr != null)? protocolExpr.evaluateString( context): "xip";
    protocol = protocol.substring( 0, 1).toUpperCase() + protocol.substring( 1);
    String className = "org.xmodel.net.nu.protocol." + protocol + "WireProtocol";
    try
    {
      Class<?> clss = ProtocolSchema.class.getClassLoader().loadClass( className);
      return new Protocol( (IWireProtocol)clss.newInstance(), new SimpleEnvelopeProtocol());
    }
    catch( Exception e)
    {
      throw new XActionException( "Unrecognized protocol.", e);
    }
  }
}
