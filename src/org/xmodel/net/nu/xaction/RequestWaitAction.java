package org.xmodel.net.nu.xaction;

import java.util.Iterator;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class RequestWaitAction extends SendAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    var = Conventions.getVarName( document.getRoot(), false);
    viaExpr = document.getExpression( "via", true);
    atExpr = document.getExpression( "at", true);
    timeoutExpr = document.getExpression( "timeout", true);
    
    onSuccessExpr = document.getExpression( "onSuccess", true);
    onErrorExpr = document.getExpression( "onError", true);
    onCompleteExpr = document.getExpression( "onComplete", true);

    if ( document.getRoot().getNumberOfChildren() == 0)
      messageExpr = document.getExpression();
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    IModelObject message = (messageExpr != null)? messageExpr.queryFirst( context): MessageSchema.getMessage( document.getRoot());
    int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
    
    IContext messageContext = new StatefulContext( context);
    context.set( "request", message);
    
    AsyncSendGroup group = new AsyncSendGroup( var, context);
    group.setReceiveScript( Conventions.getScript( document, context, onSuccessExpr));
    group.setErrorScript( Conventions.getScript( document, context, onErrorExpr));
    group.setCompleteScript( Conventions.getScript( document, context, onCompleteExpr));
    
    Iterator<ITransport> transports = MessageSchema.resolveTransport( context, viaExpr, atExpr);
    try
    {
      group.sendAndWait( transports, message, messageContext, timeout);
    }
    catch( InterruptedException e)
    {
      throw new XActionException( e);
    }

    return null;
  }

  public final static Log log = Log.getLog( RequestAction.class);
  
  private String var;
  private IExpression viaExpr;
  private IExpression atExpr;
  private IExpression messageExpr;
  private IExpression timeoutExpr;
  private IExpression onSuccessExpr;  // each
  private IExpression onErrorExpr;    // each
  private IExpression onCompleteExpr; // all
}
