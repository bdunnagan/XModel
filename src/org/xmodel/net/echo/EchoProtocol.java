package org.xmodel.net.echo;

import java.util.concurrent.Executor;

import org.xmodel.net.HeaderProtocol;

public class EchoProtocol
{
  public EchoProtocol( HeaderProtocol headerProtocol, Executor executor)
  {
    this.headerProtocol = headerProtocol;
    this.requestProtocol = new EchoRequestProtocol( this);
    this.responseProtocol = new EchoResponseProtocol( this);
    this.executor = executor;
  }
  
  public HeaderProtocol headerProtocol;
  public EchoRequestProtocol requestProtocol;
  public EchoResponseProtocol responseProtocol;
  public Executor executor;
}
