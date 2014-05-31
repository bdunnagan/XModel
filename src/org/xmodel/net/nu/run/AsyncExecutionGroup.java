package org.xmodel.net.nu.run;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xaction.IXAction;
import org.xmodel.xpath.expression.IContext;

public class AsyncExecutionGroup
{
  public AsyncExecutionGroup( IContext context, List<ITransport> transports, IXAction onSuccess, IXAction onError, IXAction onComplete, ScheduledExecutorService scheduler)
  {
    this.callingContext = context;
    this.onSuccess = onSuccess;
    this.onError = onError;
    this.onComplete = onComplete;
    this.scheduler = scheduler;
    
    executions = new HashSet<AsyncExecution>( transports.size());
    for( ITransport transport: transports)
    {
      executions.add( new AsyncExecution( this, transport));
    }
  }
  
  public ScheduledExecutorService scheduler()
  {
    return scheduler;
  }

  protected void notifySuccess( AsyncExecution execution, IContext context, IModelObject response)
  {
    if ( onSuccess != null)
    {
      try
      {
        context.getScope().set( var, response);
        onSuccess.run( context);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
    
    if ( executionComplete( execution))
      notifyComplete();
  }
  
  protected void notifyError( AsyncExecution execution, IContext context, Object error)
  {
    if ( onError != null)
    {
      try
      {
        context.getScope().set( var, error);
        onError.run( context);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
    
    if ( executionComplete( execution))
      notifyComplete();
  }
  
  private boolean executionComplete( AsyncExecution execution)
  {
    synchronized( executions)
    {
      executions.remove( execution);
      return executions.isEmpty();
    }
  }
  
  private void notifyComplete()
  {
    if ( onComplete != null)
    {
      try
      {
        onComplete.run( callingContext);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }

  public static Log log = Log.getLog( AsyncExecutionGroup.class);

  private String var;
  private IContext callingContext;
  private Set<AsyncExecution> executions;
  private IXAction onSuccess;
  private IXAction onError;
  private IXAction onComplete;
  private ScheduledExecutorService scheduler;
}
