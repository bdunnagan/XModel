package org.xmodel.net.nu.xaction;

import java.util.Iterator;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class RequestAction extends GuardedAction
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
    if ( message != null)
    {
      int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
      int life = (lifeExpr != null)? (int)lifeExpr.evaluateNumber( context): -1;
      int retries = (retriesExpr != null)? (int)retriesExpr.evaluateNumber( context): (life >= 0)? 0: -1;
      
      IContext messageContext = new StatefulContext( context);
      
      AsyncSendGroup group = new AsyncSendGroup( context);
      group.setReceiveScript( Conventions.getScript( document, context, onReceiveExpr));
      group.setErrorScript( Conventions.getScript( document, context, onErrorExpr));
      group.setCompleteScript( Conventions.getScript( document, context, onCompleteExpr));
      
      Iterator<ITransport> transports = ActionUtil.resolveTransport( context, viaExpr, toExpr);
      group.send( transports, message, false, messageContext, timeout, retries, life);
    }

    return null;
  }

  public final static Log log = Log.getLog( RequestAction.class);
  
  private IExpression viaExpr;
  private IExpression toExpr;
  private IExpression messageExpr;
  private IExpression timeoutExpr;
  private IExpression onReceiveExpr;  // each
  private IExpression onErrorExpr;    // each
  private IExpression onCompleteExpr; // all
  private IExpression retriesExpr;
  private IExpression lifeExpr;
}
