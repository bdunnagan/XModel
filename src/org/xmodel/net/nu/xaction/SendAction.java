package org.xmodel.net.nu.xaction;

import java.util.Iterator;
import org.xmodel.IModelObject;
import org.xmodel.net.nu.ITransport;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class SendAction extends GuardedAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    viaExpr = document.getExpression( "via", true);
    atExpr = document.getExpression( "at", true);
    onErrorExpr = document.getExpression( "onError", true);

    if ( document.getRoot().getNumberOfChildren() == 0)
      messageExpr = document.getExpression();
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    IModelObject message = (messageExpr != null)? messageExpr.queryFirst( context): MessageSchema.getMessage( document.getRoot());
    
    AsyncSendGroup group = new AsyncSendGroup( null, context);
    group.setErrorScript( Conventions.getScript( document, context, onErrorExpr));
    
    Iterator<ITransport> transports = MessageSchema.resolveTransport( context, viaExpr, atExpr);
    group.send( transports, message);

    return null;
  }

  private IExpression viaExpr;
  private IExpression atExpr;
  private IExpression messageExpr;
  private IExpression onErrorExpr;
}
