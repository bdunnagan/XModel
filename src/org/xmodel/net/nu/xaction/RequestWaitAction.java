package org.xmodel.net.nu.xaction;

import java.util.Iterator;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class RequestWaitAction extends GuardedAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    viaExpr = document.getExpression( "via", true);
    toExpr = document.getExpression( "to", true);
    timeoutExpr = document.getExpression( "timeout", true);
    retriesExpr = document.getExpression( "retries", true);
    lifeExpr = document.getExpression( "life", true);
    
    onReceiveExpr = document.getExpression( "onReceive", true);
    onErrorExpr = document.getExpression( "onError", true);
    onCompleteExpr = document.getExpression( "onComplete", true);

    if ( document.getRoot().getNumberOfChildren() == 0)
      messageExpr = document.getExpression();
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    IModelObject message = (messageExpr != null)? messageExpr.queryFirst( context): ActionUtil.getMessage( document.getRoot());
    int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
    int life = (lifeExpr != null)? (int)lifeExpr.evaluateNumber( context): -1;
    int retries = (retriesExpr != null)? (int)retriesExpr.evaluateNumber( context): (life >= 0)? 0: -1;
    
    IContext messageContext = new StatefulContext( context);
    context.set( "request", message);
    
    final IXAction onReceive = Conventions.getScript( document, context, onReceiveExpr);
    final IXAction onError = Conventions.getScript( document, context, onErrorExpr);
    final IXAction onComplete = Conventions.getScript( document, context, onCompleteExpr);
    
    AsyncSendGroup group = new AsyncSendGroup( context) {
      @Override
      protected void onSuccess( ITransport transport, IContext messageContext, IModelObject message, IModelObject request)
      {
        if ( onReceive != null) 
        {
          ModelObject transportNode = new ModelObject( "transport");
          transportNode.setValue( transport);
          
          synchronized( messageContext)
          {
            ScriptAction.passVariables( new Object[] { transportNode, message}, messageContext, onReceive);
          }
          
          onReceive.run( messageContext);
        }
      }
      
      @Override
      protected void onError( ITransport transport, IContext context, Error error, IModelObject request)
      {
        if ( onError != null)
        {
          ModelObject transportNode = new ModelObject( "transport");
          transportNode.setValue( transport);

          IContext errorContext = new StatefulContext( context);
          ScriptAction.passVariables( new Object[] { transport, error.toString()}, errorContext, onError);
          onError.run( errorContext);
        }
      }
      
      @Override
      protected void onComplete( IContext callContext)
      {
        if ( onComplete != null) onComplete.run( callContext);
      }
    };
    
    Iterator<ITransport> transports = ActionUtil.resolveTransport( context, viaExpr, toExpr);
    try
    {
      group.sendAndWait( transports, message, false, messageContext, timeout, retries, life);
    }
    catch( InterruptedException e)
    {
      throw new XActionException( "Blocking request interrupted...", e);
    }

    return null;
  }

  public final static Log log = Log.getLog( RequestAction.class);

  private IExpression viaExpr;
  private IExpression toExpr;
  private IExpression messageExpr;
  private IExpression timeoutExpr;
  private IExpression retriesExpr;
  private IExpression lifeExpr;
  private IExpression onReceiveExpr;  // each
  private IExpression onErrorExpr;    // each
  private IExpression onCompleteExpr; // all
}
