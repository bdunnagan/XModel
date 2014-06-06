package org.xmodel.net.nu;

import org.xmodel.xpath.expression.IContext;

public interface IErrorListener
{
  public void onError( ITransport transport, IContext context, ITransport.Error error) throws Exception;
}
