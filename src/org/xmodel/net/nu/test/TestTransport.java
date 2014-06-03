package org.xmodel.net.nu.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.future.SuccessAsyncFuture;
import org.xmodel.net.nu.AbstractTransport;
import org.xmodel.net.nu.IProtocol;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xpath.expression.IContext;

public class TestTransport extends AbstractTransport
{
  public TestTransport( IProtocol protocol, IContext transportContext)
  {
    super( protocol, transportContext, Executors.newScheduledThreadPool( 1));
    transports.add( this);
  }

  @Override
  public AsyncFuture<ITransport> connect( int timeout) throws IOException
  {
    return null;
  }

  @Override
  public AsyncFuture<ITransport> disconnect() throws IOException
  {
    return null;
  }

  @Override
  public AsyncFuture<ITransport> send( IModelObject message) throws IOException
  {
    for( TestTransport transport: transports)
    {
      if ( transport == this) continue;
      
    }
    return new SuccessAsyncFuture<ITransport>( this);
  }
  
  private static List<TestTransport> transports = new ArrayList<TestTransport>();
}
