package org.xmodel.net.execution;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.net.HeaderProtocol;
import org.xmodel.xpath.expression.IContext;

public class ExecutionProtocol
{
  public ExecutionProtocol( HeaderProtocol headerProtocol, IContext context, ScheduledExecutorService scheduler, Executor executor)
  {
    this.context = context;
    this.executor = executor;
    this.headerProtocol = headerProtocol;
    this.requestProtocol = new ExecutionRequestProtocol( this);
    this.responseProtocol = new ExecutionResponseProtocol( this);
    this.scheduler = scheduler;
    this.requestCompressor = new TabularCompressor( false);
    this.responseCompressor = new TabularCompressor( false);
  }
  
  /**
   * Reset this instance by releasing internal resources.  This method should be called after 
   * the channel is closed to prevent conflict between protocol traffic and the freeing of resources.
   */
  public void reset()
  {
    headerProtocol.reset();
    requestProtocol.reset();
    responseProtocol.reset();
  }
  
  public IContext context;
  public Executor executor;
  public HeaderProtocol headerProtocol;
  public ExecutionRequestProtocol requestProtocol;
  public ExecutionResponseProtocol responseProtocol;
  public ScheduledExecutorService scheduler;
  public ICompressor requestCompressor;
  public ICompressor responseCompressor;
}
