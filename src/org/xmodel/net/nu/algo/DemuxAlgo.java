package org.xmodel.net.nu.algo;

import java.util.concurrent.ConcurrentHashMap;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.DefaultEventHandler;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.xpath.expression.IContext;

public class DemuxAlgo extends DefaultEventHandler
{
  public DemuxAlgo( ConcurrentHashMap<Long, ITransportImpl> channels)
  {
    this.channels = channels;
    log.setLevel( Log.all);
  }
  
  @Override
  public boolean notifyReceive( ITransportImpl transport, IModelObject envelope)
  {
    Object object = transport.getProtocol().envelope().getChannel( envelope);
    if ( object != null)
    {
      Long channel = (object instanceof Number)? ((Number)object).longValue(): Long.parseLong( object.toString());  
      ITransportImpl channelTransport = channels.get( channel);
      if ( channelTransport != null) 
      {
        return channelTransport.getEventPipe().notifyReceive( channelTransport, envelope);
      }
    }
    return false;
  }
  
  @Override
  public boolean notifyError( ITransportImpl transport, IContext context, Error error, IModelObject request)
  {
    if ( request != null)
    {
      Object channel = transport.getProtocol().envelope().getChannel( request);
      if ( channel != null)
      {
        ITransportImpl channelTransport = channels.get( channel);
        if ( channelTransport != null) 
        {
          return channelTransport.getEventPipe().notifyError( channelTransport, context, error, request);
        }
      }
    }
    return false;
  }

  public final static Log log = Log.getLog( DemuxAlgo.class);
  
  private ConcurrentHashMap<Long, ITransportImpl> channels;
}
