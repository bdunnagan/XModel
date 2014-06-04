package org.xmodel.net.nu;

import java.io.IOException;

import org.xmodel.IModelObject;
import org.xmodel.log.SLog;

final class ThreadSafeProtocol implements IProtocol
{
  public ThreadSafeProtocol( IProtocol protocol)
  {
    this.protocol = protocol;
    this.threads = new ThreadLocal<IProtocol>();
  }
  
  @Override
  public byte[] encode( IModelObject message) throws IOException
  {
    return getThreadProtocol().encode( message);
  }

  @Override
  public IModelObject decode( byte[] bytes, int offset, int length) throws IOException
  {
    return getThreadProtocol().decode( bytes, offset, length);
  }
  
  private IProtocol getThreadProtocol()
  {
    IProtocol protocol = threads.get();
    if ( protocol == null)
    {
      try
      {
        protocol = (IProtocol)this.protocol.getClass().newInstance();
        threads.set( protocol);
      }
      catch( Exception e)
      {
        SLog.exception( this, e);
      }
    }
    return protocol;
  }

  private IProtocol protocol;
  private ThreadLocal<IProtocol> threads;
}
