package org.xmodel.net.register;

import org.xmodel.net.HeaderProtocol;
import org.xmodel.net.IXioPeerRegistry;

public class RegisterProtocol
{
  public RegisterProtocol( IXioPeerRegistry registry, HeaderProtocol headerProtocol)
  {
    this.registry = registry;
    this.headerProtocol = headerProtocol;
    this.registerRequestProtocol = new RegisterRequestProtocol( this);
    this.unregisterRequestProtocol = new UnregisterRequestProtocol( this);
  }
  
  public IXioPeerRegistry registry;
  public HeaderProtocol headerProtocol;
  public RegisterRequestProtocol registerRequestProtocol;
  public UnregisterRequestProtocol unregisterRequestProtocol;
}
