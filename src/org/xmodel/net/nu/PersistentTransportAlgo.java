package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.xmodel.IModelObject;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.xpath.expression.IContext;

public class PersistentTransportAlgo implements IEventHandler, Runnable
{
  public PersistentTransportAlgo( ITransportImpl transport)
  {
    this.transport = transport;
    this.retryMinDelay = 1000;
    this.retryMaxDelay = 10000;
    this.retryDelay = retryMinDelay;
  }

  @Override
  public void run()
  {
    transport.connect();
  }
  
  @Override
  public boolean notifySend( IModelObject envelope, IContext messageContext, int timeout, int retries, int life)
  {
    return false;
  }

  @Override
  public boolean notifyReceive( ByteBuffer buffer) throws IOException
  {
    return false;
  }

  public boolean notifyReceive( IModelObject envelope)
  {
    return false;
  }

  @Override
  public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage)
  {
    return false;
  }

  @Override
  public boolean notifyConnect( IContext transportContext) throws IOException
  {
    retryDelay = retryMinDelay;
    return false;
  }

  @Override
  public boolean notifyDisconnect( IContext transportContext) throws IOException
  {
    this.transport.schedule( this, retryDelay);
    increaseRetryDelay();
    return false;
  }

  private void increaseRetryDelay()
  {
    retryDelay *= 2;
    if ( retryDelay > retryMaxDelay) retryDelay = retryMaxDelay;
  }
  
  @Override
  public boolean notifyRegister( IContext transportContext, String name)
  {
    return false;
  }

  @Override
  public boolean notifyDeregister( IContext transportContext, String name)
  {
    return false;
  }

  @Override
  public boolean notifyError( IContext context, Error error, IModelObject request)
  {
    switch( error)
    {
      case connectRefused:
      case connectError:
        this.transport.schedule( this, retryDelay);
        increaseRetryDelay();
        break;
      
      default:
        break;
    }
    
    return false;
  }

  @Override
  public boolean notifyException( IOException e)
  {
    return false;
  }

  private ITransportImpl transport;
  private int retryDelay;
  private int retryMinDelay;
  private int retryMaxDelay;
}
