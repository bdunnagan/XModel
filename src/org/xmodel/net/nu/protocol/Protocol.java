package org.xmodel.net.nu.protocol;

public class Protocol
{
  public Protocol( IWireProtocol wire, IEnvelopeProtocol envelope)
  {
    this.wire = wire;
    this.envelope = envelope;
  }
  
  public IWireProtocol wire()
  {
    return wire;
  }
  
  public IEnvelopeProtocol envelope()
  {
    return envelope;
  }
  
  private IWireProtocol wire;
  private IEnvelopeProtocol envelope;
}
