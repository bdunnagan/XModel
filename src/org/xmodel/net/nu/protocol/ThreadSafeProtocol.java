package org.xmodel.net.nu.protocol;

import org.xmodel.log.SLog;

public final class ThreadSafeProtocol extends Protocol
{
  public ThreadSafeProtocol( IWireProtocol wire, IEnvelopeProtocol envelope)
  {
    super( wire, envelope);
    this.threads = new ThreadLocal<Protocol>();
  }

  @Override
  public IWireProtocol wire()
  {
    return getThreadProtocol().wire();
  }

  @Override
  public IEnvelopeProtocol envelope()
  {
    return getThreadProtocol().envelope();
  }

  public Protocol clone()
  {
    throw new UnsupportedOperationException();
  }

  private Protocol getThreadProtocol()
  {
    Protocol protocol = threads.get();
    if ( protocol == null)
    {
      try
      {
        IWireProtocol wireClone = super.wire().getClass().newInstance();
        IEnvelopeProtocol envelopeClone = super.envelope().getClass().newInstance();
        protocol = new Protocol( wireClone, envelopeClone);
        threads.set( protocol);
      }
      catch( Exception e)
      {
        SLog.exception( this, e);
      }
    }
    return protocol;
  }

  private ThreadLocal<Protocol> threads;
}
