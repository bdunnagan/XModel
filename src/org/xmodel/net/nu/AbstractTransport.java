package org.xmodel.net.nu;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.net.nu.protocol.ThreadSafeProtocol;
import org.xmodel.util.HexDump;
import org.xmodel.xpath.expression.IContext;

public abstract class AbstractTransport extends DefaultEventHandler implements ITransportImpl
{
  protected AbstractTransport( Protocol protocol, IContext transportContext)
  {
    this.protocol = new ThreadSafeProtocol( protocol.wire(), protocol.envelope());
    this.transportContext = transportContext;
    
    eventPipe = new EventPipe();
    eventPipe.addLast( this);    
    
    log.setLevel( Log.all);
  }
  
  @Override
  public AsyncFuture<ITransport> send( IModelObject envelope, IContext messageContext, int timeout, int retries, int life)
  {
    eventPipe.notifySend( envelope, messageContext, timeout, retries, life);
    return sendImpl( envelope, null);
  }

  @Override
  public AsyncFuture<ITransport> sendAck( IModelObject envelope)
  {
    return sendImpl( protocol.envelope().buildAck( envelope), envelope);
  }

  @Override
  public EventPipe getEventPipe()
  {
    return eventPipe;
  }

  @Override
  public Protocol getProtocol()
  {
    return protocol;
  }
  
  @Override
  public IContext getTransportContext()
  {
    return transportContext;
  }
  
  @Override
  public boolean notifyReceive( ByteBuffer buffer) throws IOException
  {
    if ( log.verbose()) log.verbosef( "Read buffer contains:\n%s", HexDump.toString( Unpooled.wrappedBuffer( buffer)));
    
    try
    {
      // decode
      IModelObject envelope = protocol.wire().decode( buffer);
      if ( envelope != null)
      {
        // deliver
        eventPipe.notifyReceive( envelope);
        return true;
      }
    }
    catch( IOException e)
    {
      eventPipe.notifyException( e);
    }
    
    return false;
  }
  
  @Override
  public boolean notifyReceive( IModelObject envelope)
  {
    String route = protocol.envelope().getRoute( envelope);
    if ( route == null)
    {
      eventPipe.notifyReceive( envelope, transportContext, null);
      return true;
    }
    else
    {
      // TODO
      throw new UnsupportedOperationException();
    }
  }
  
  @Override
  public boolean notifyException( IOException e)
  {
    log.exception( e);
    return false;
  }

  public final static Log log = Log.getLog( AbstractTransport.class);
 
  private Protocol protocol;
  private IContext transportContext;
  private EventPipe eventPipe;
}