package org.xmodel.net.execution;

import java.util.concurrent.ScheduledExecutorService;
import org.xmodel.IDispatcher;
import org.xmodel.net.bind.SyncResponseProtocol;
import org.xmodel.net.nu.ErrorProtocol;
import org.xmodel.net.nu.HeaderProtocol;
import org.xmodel.xpath.expression.IContext;

public class ExecutionProtocol
{
  public ExecutionProtocol( HeaderProtocol headerProtocol, ErrorProtocol errorProtocol, IContext context, IDispatcher dispatcher, ScheduledExecutorService scheduler)
  {
    this.context = context;
    this.dispatcher = dispatcher;
    this.headerProtocol = headerProtocol;
    this.errorProtocol = errorProtocol;
    this.requestProtocol = new ExecutionRequestProtocol( this);
    this.responseProtocol = new ExecutionResponseProtocol( this);
    this.scheduler = scheduler;
  }
  
  public IContext context;
  public IDispatcher dispatcher;
  public HeaderProtocol headerProtocol;
  public ErrorProtocol errorProtocol;
  public ExecutionRequestProtocol requestProtocol;
  public ExecutionResponseProtocol responseProtocol;
  public ScheduledExecutorService scheduler;
}
