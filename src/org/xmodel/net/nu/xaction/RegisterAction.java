package org.xmodel.net.nu.xaction;

import java.util.Iterator;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class RegisterAction extends GuardedAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    nameExpr = document.getExpression( "name", true);
    viaExpr = document.getExpression( "via", true);
    toExpr = document.getExpression( "to", true);
    timeoutExpr = document.getExpression( "timeout", true);
    retriesExpr = document.getExpression( "retries", true);
    lifeExpr = document.getExpression( "life", true);
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    String name = nameExpr.evaluateString( context);
    int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
    int life = (lifeExpr != null)? (int)lifeExpr.evaluateNumber( context): -1;
    int retries = (retriesExpr != null)? (int)retriesExpr.evaluateNumber( context): (life >= 0)? 0: -1;
    
    IContext messageContext = new StatefulContext( context);
    
    Iterator<ITransport> iter = ActionUtil.resolveTransport( context, viaExpr, toExpr);
    while( iter.hasNext())
    {
      ITransport transport = iter.next();
      IModelObject envelope = transport.getProtocol().envelope().buildRegisterEnvelope( name, life);
      transport.send( envelope, messageContext, timeout, retries, life);
    }

    return null;
  }

  public final static Log log = Log.getLog( RegisterAction.class);
  
  private IExpression nameExpr;
  private IExpression viaExpr;
  private IExpression toExpr;
  private IExpression timeoutExpr;
  private IExpression retriesExpr;
  private IExpression lifeExpr;
}
