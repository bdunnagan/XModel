package org.xmodel.net.nu.xaction;

import java.io.IOException;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.net.nu.DefaultEventHandler;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

class EventHandlerAdapter extends DefaultEventHandler
{
  public EventHandlerAdapter( XActionDocument document, ITransport transport, IContext context)
  {
    this.transport = transport;
    this.context = context;
    this.document = document;
    
    onConnectExpr = document.getExpression( "onConnect", true);
    onDisconnectExpr = document.getExpression( "onDisconnect", true);
    onRegisterExpr = document.getExpression( "onRegister", true);
    onDeregisterExpr = document.getExpression( "onDeregister", true);
    onReceiveExpr = document.getExpression( "onReceive", true);
    onErrorExpr = document.getExpression( "onError", true);
  }
  
  @Override
  public boolean notifyConnect( ITransportImpl transport, IContext transportContext) throws IOException
  {
    IXAction onConnect = Conventions.getScript( document, context, onConnectExpr);
    if ( onConnect != null) 
    {
      StatefulContext connectContext = new StatefulContext( transportContext);
      
      ModelObject transportNode = new ModelObject( "transport");
      transportNode.setValue( transport);
      
      ScriptAction.passVariables( new Object[] { transportNode}, connectContext, onConnect);
      onConnect.run( connectContext);
    }
    return false;
  }

  @Override
  public boolean notifyDisconnect( ITransportImpl transport, IContext transportContext) throws IOException
  {
    IXAction onDisconnect = Conventions.getScript( document, context, onDisconnectExpr);
    if ( onDisconnect != null) 
    {
      StatefulContext disconnectContext = new StatefulContext( transportContext);
      
      ModelObject transportNode = new ModelObject( "transport");
      transportNode.setValue( transport);
      
      ScriptAction.passVariables( new Object[] { transportNode}, disconnectContext, onDisconnect);
      onDisconnect.run( disconnectContext);
    }
    return false;
  }

  @Override
  public boolean notifyReceive( ITransportImpl transport, IModelObject envelope, IContext messageContext, IModelObject requestEnvelope)
  {
    // ignore acks
    if ( envelope == null) return false;
    
    IXAction onReceive = Conventions.getScript( document, messageContext, onReceiveExpr);
    if ( onReceive != null) 
    {
      ModelObject transportNode = new ModelObject( "transport");
      transportNode.setValue( transport);
      
      synchronized( messageContext)
      {
        ScriptAction.passVariables( new Object[] { transportNode, unwrap( envelope)}, messageContext, onReceive);
      }
      
      onReceive.run( messageContext);
    }
    return false;
  }

  @Override
  public boolean notifyRegister( ITransportImpl transport, IContext transportContext, String name)
  {
    IXAction onRegister = Conventions.getScript( document, transportContext, onRegisterExpr);
    if ( onRegister != null) 
    {
      ModelObject transportNode = new ModelObject( "transport");
      transportNode.setValue( transport);
      
      synchronized( transportContext)
      {
        ScriptAction.passVariables( new Object[] { transportNode, name}, transportContext, onRegister);
      }
      
      onRegister.run( transportContext);
    }
    return false;
  }

  @Override
  public boolean notifyDeregister( ITransportImpl transport, IContext transportContext, String name)
  {
    IXAction onDeregister = Conventions.getScript( document, transportContext, onDeregisterExpr);
    if ( onDeregister != null) 
    {
      ModelObject transportNode = new ModelObject( "transport");
      transportNode.setValue( transport);
      
      synchronized( transportContext)
      {
        ScriptAction.passVariables( new Object[] { transportNode, name}, transportContext, onDeregister);
      }
      
      onDeregister.run( transportContext);
    }
    return false;
  }

  @Override
  public boolean notifyError( ITransportImpl transport, IContext context, Error error, IModelObject requestEnvelope)
  {
    IXAction onError = Conventions.getScript( document, context, onErrorExpr);
    if ( onError != null) 
    {
      StatefulContext messageContext = new StatefulContext( context);
      
      ModelObject transportNode = new ModelObject( "transport");
      transportNode.setValue( transport);

      synchronized( messageContext)
      {
        ScriptAction.passVariables( new Object[] { transport, error.toString(), unwrap( requestEnvelope)}, messageContext, onError);
      }
      
      onError.run( messageContext);
    }
    return false;
  }

  @Override
  public boolean notifyException( ITransportImpl transport, IOException e)
  {
    return false;
  }
  
  private Object unwrap( IModelObject envelope)
  {
    return transport.getProtocol().envelope().getMessage( envelope);
  }
  
  private ITransport transport;
  private IContext context;
  private XActionDocument document;
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
  private IExpression onRegisterExpr;
  private IExpression onDeregisterExpr;
  private IExpression onReceiveExpr;
  private IExpression onErrorExpr;
}