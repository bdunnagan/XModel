package org.xmodel.net.nu.algo;

import java.util.concurrent.ConcurrentHashMap;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.DefaultEventHandler;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol;
import org.xmodel.xpath.expression.IContext;

public class MuxAlgo extends DefaultEventHandler
{
  public MuxAlgo( ConcurrentHashMap<Long, ITransportImpl> channels)
  {
    this.channels = channels;
    log.setLevel( Log.all);
  }
  
  @Override
  public boolean notifySend( ITransportImpl transport, IModelObject envelope, IContext messageContext, int timeout, int retries, int life)
  {
    IEnvelopeProtocol envelopeProtocol = transport.getProtocol().envelope(); 
    long channel = transport.hashCode();
    envelopeProtocol.setChannel( envelope, channel);
    channels.putIfAbsent( channel, transport);
    return false;
  }

  public final static Log log = Log.getLog( MuxAlgo.class);
  
  private ConcurrentHashMap<Long, ITransportImpl> channels;
}
