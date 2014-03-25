package org.xmodel.net.execution;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import org.xmodel.compress.ICompressor;
import org.xmodel.net.HeaderProtocol;
import org.xmodel.xpath.expression.IContext;

public class ExecutionProtocol
{
  public ExecutionProtocol( 
      HeaderProtocol headerProtocol, 
      IContext context, 
      Executor executor, 
      ScheduledExecutorService scheduler,
      ExecutionPrivilege privilege)
  {
    this.context = context;
    this.executor = executor;
    this.headerProtocol = headerProtocol;
    this.requestProtocol = new ExecutionRequestProtocol( this, privilege);
    this.responseProtocol = new ExecutionResponseProtocol( this);
    this.scheduler = scheduler;
    
    //
    // When the worker pool of both the client and the server only has one thread, these
    // can be set to new TabularCompressor( true).  Otherwise, the compressors are used
    // by multiple threads, and a progressive tag table cannot be maintained.
    //
    //this.requestCompressor = new TabularCompressor( false);
    //this.responseCompressor = new TabularCompressor( false);
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
