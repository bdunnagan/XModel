package org.xmodel.net.nu.xaction;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class RespondAction extends GuardedAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    viaExpr = document.getExpression( "via", true);
    
    requestExpr = document.getExpression( "request", true);

    if ( document.getRoot().getNumberOfChildren() == 0)
      messageExpr = document.getExpression();
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    IModelObject viaNode = viaExpr.queryFirst( context);
    IModelObject request = requestExpr.queryFirst( context);
    IModelObject message = (messageExpr != null)? getMessage( context): ActionUtil.getMessage( document.getRoot());
    if ( message != null)
    {
      Object object = viaNode.getValue();
      if ( object != null && object instanceof ITransport)
      {
        ITransport transport = ((ITransport)object);
        IEnvelopeProtocol envelopeProtocol = transport.getProtocol().envelope();
        IModelObject requestEnvelope = envelopeProtocol.getEnvelope( request);
        IModelObject responseEnvelope = envelopeProtocol.buildResponseEnvelope( requestEnvelope, message);
        transport.send( responseEnvelope, null, -1, -1, -1);
      }
    }

    return null;
  }
  
  private IModelObject getMessage( IContext context)
  {
    switch( messageExpr.getType( context))
    {
      case NODES:
      {
        return messageExpr.queryFirst( context);
      }
        
      case NUMBER:
      {
        IModelObject message = new ModelObject( "message");
        message.setValue( messageExpr.evaluateNumber( context));
        return message;
      }
      
      case BOOLEAN:
      {
        IModelObject message = new ModelObject( "message");
        message.setValue( messageExpr.evaluateBoolean( context));
        return message;
      }
      
      case STRING:
      {
        IModelObject message = new ModelObject( "message");
        message.setValue( messageExpr.evaluateString( context));
        return message;
      }
      
      default:
        return null;
    }
  }
  
  public final static Log log = Log.getLog( RespondAction.class);
  
  private IExpression viaExpr;
  private IExpression messageExpr;
  private IExpression requestExpr;
}
