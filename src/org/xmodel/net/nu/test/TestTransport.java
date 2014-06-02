package org.xmodel.net.nu.test;

import java.io.IOException;
import java.util.concurrent.Executors;

import org.xmodel.IModelObject;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.nu.AbstractTransport;
import org.xmodel.net.nu.IProtocol;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xpath.expression.IContext;

public class TestTransport extends AbstractTransport
{
  public TestTransport( IProtocol protocol, IContext transportContext)
  {
    super( protocol, transportContext, Executors.newScheduledThreadPool( 1));
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
    return null;
  }
}
