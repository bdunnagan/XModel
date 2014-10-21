package org.xmodel.net.nu;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.FailureAsyncFuture;
import org.xmodel.log.Log;
import org.xmodel.net.nu.protocol.Protocol;
import org.xmodel.net.nu.protocol.ThreadSafeProtocol;
import org.xmodel.util.HexDump;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;

public abstract class AbstractTransport extends DefaultEventHandler implements ITransportImpl
{
  protected AbstractTransport( Protocol protocol, IContext transportContext)
  {
    this.protocol = new ThreadSafeProtocol( protocol.wire(), protocol.envelope());
    this.transportContext = transportContext;
    
    eventPipe = new EventPipe();
    eventPipe.addLast( this);    
  }
  
  @Override
  public AsyncFuture<ITransport> send( IModelObject request, IModelObject envelope, IContext messageContext, int timeout, int retries, int life)
  {
    try
    {
      eventPipe.notifySend( this, envelope, messageContext, timeout, retries, life);
      return sendImpl( envelope, request);
    }
    catch( IOException e)
    {
      log.exception( e);
      return new FailureAsyncFuture<ITransport>( this, e);
    }
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
  public boolean notifySend( ITransportImpl transport, IModelObject envelope, IContext messageContext, int timeout, int retries, int life)
  {
    if ( log.verbose()) log.verbosef( "Sending message:\n%s", XmlIO.write( Style.printable, envelope));
    return false;
  }

  @Override
  public boolean notifyReceive( ITransportImpl transport, ByteBuffer buffer) throws IOException
  {
    if ( log.verbose()) log.verbosef( "Read buffer contains:\n%s", HexDump.toString( Unpooled.wrappedBuffer( buffer)));
    
    try
    {
      // decode
      IModelObject envelope = protocol.wire().decode( buffer);
      if ( envelope != null)
      {
        // deliver
        eventPipe.notifyReceive( transport, envelope);
        return true;
      }
    }
    catch( IOException e)
    {
      eventPipe.notifyException( transport, e);
    }
    
    return false;
  }
  
  @Override
  public boolean notifyReceive( ITransportImpl transport, IModelObject envelope)
  {
    String route = protocol.envelope().getRoute( envelope);
    if ( route == null)
    {
      eventPipe.notifyReceive( this, envelope, transportContext, null);
      return true;
    }
    else
    {
      // TODO
      throw new UnsupportedOperationException();
    }
  }
  
  @Override
  public boolean notifyException( ITransportImpl transport, IOException e)
  {
    log.exception( e);
    return false;
  }

  public final static Log log = Log.getLog( AbstractTransport.class);
 
  private Protocol protocol;
  private IContext transportContext;
  private EventPipe eventPipe;
}