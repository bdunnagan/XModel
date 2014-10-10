package org.xmodel.net.nu.xaction;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.tcp.ITcpServerEventHandler;
import org.xmodel.net.nu.tcp.TcpServerRouter;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class TcpServerAction extends GuardedAction implements ITcpServerEventHandler
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    var = Conventions.getVarName( document.getRoot(), true);
    bindHostExpr = document.getExpression( "bindHost", true);
    bindPortExpr = document.getExpression( "bindPort", true);
    protocolExpr = document.getExpression( "protocol", true);
    schedulerExpr = document.getExpression( "scheduler", true);
    reliableExpr = document.getExpression( "reliable", true);
    heartbeatPeriodExpr = document.getExpression( "heartbeatPeriod", true);
    heartbeatTimeoutExpr = document.getExpression( "heartbeatTimeout", true);
    onRegisterExpr = document.getExpression( "onRegister", true);
    onDeregisterExpr = document.getExpression( "onDeregister", true);
    onReceiveExpr = document.getExpression( "onReceive", true);
    onErrorExpr = document.getExpression( "onError", true);
    onConnectExpr = document.getExpression( "onConnect", true);
    onDisconnectExpr = document.getExpression( "onDisconnect", true);
  }
  
  @Override
  protected Object[] doAction( IContext context)
  {
    String bindHost = (bindHostExpr != null)? bindHostExpr.evaluateString( context): null;
    int bindPort = (bindPortExpr != null)? (int)bindPortExpr.evaluateNumber( context): 0;
        
    ScheduledExecutorService scheduler = (schedulerExpr != null)? (ScheduledExecutorService)Conventions.getCache( context, schedulerExpr): null;
    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1);

    boolean reliable = (reliableExpr != null)? reliableExpr.evaluateBoolean( context): true;
    
    try
    {
      TcpServerRouter server = new TcpServerRouter( ProtocolSchema.getProtocol( protocolExpr, context), context, scheduler, reliable);
      Conventions.putCache( context, var, server);
 
      int heartbeatPeriod = (heartbeatPeriodExpr != null)? (int)heartbeatPeriodExpr.evaluateNumber( context): 10000;
      int heartbeatTimeout = (heartbeatTimeoutExpr != null)? (int)heartbeatTimeoutExpr.evaluateNumber( context): 30000;
      server.setHeartbeat( heartbeatPeriod, heartbeatTimeout);
      
      server.setEventHandler( this);
      server.start( new InetSocketAddress( bindHost, bindPort));
    }
    catch( Exception e)
    {
      throw new XActionException( e);
    }
    
    return null;
  }
  
  @Override
  public void notifyConnect( ITransport transport, IContext transportContext) throws IOException
  {
    IXAction onConnect = Conventions.getScript( document, transportContext, onConnectExpr);
    if ( onConnect != null) onConnect.run( transportContext);
  }
  
  @Override
  public void notifyDisconnect( ITransport transport, IContext transportContext) throws IOException
  {
    IXAction onDisconnect = Conventions.getScript( document, transportContext, onDisconnectExpr);
    if ( onDisconnect != null) onDisconnect.run( transportContext);
  }
  
  @Override
  public void notifyReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject requestMessage)
  {
    // ignore acks
    if ( message == null) return;
    
    IXAction onReceive = Conventions.getScript( document, messageContext, onReceiveExpr);
    if ( onReceive != null) 
    {
      ModelObject transportNode = new ModelObject( "transport");
      transportNode.setValue( transport);
      
      ScriptAction.passVariables( new Object[] { transportNode, unwrap( transport, message), unwrap( transport, requestMessage)}, messageContext, onReceive);
      onReceive.run( messageContext);
    }
  }
  
  @Override
  public boolean notifyRegister( ITransport transport, IContext transportContext, String name)
  {
    IXAction onRegister = Conventions.getScript( document, transportContext, onRegisterExpr);
    if ( onRegister != null) 
    {
      ModelObject transportNode = new ModelObject( "transport");
      transportNode.setValue( transport);
      
      ScriptAction.passVariables( new Object[] { transportNode, name}, transportContext, onRegister);
      onRegister.run( transportContext);
    }
    return false;
  }

  @Override
  public boolean notifyDeregister( ITransport transport, IContext transportContext, String name)
  {
    IXAction onDeregister = Conventions.getScript( document, transportContext, onDeregisterExpr);
    if ( onDeregister != null) 
    {
      ModelObject transportNode = new ModelObject( "transport");
      transportNode.setValue( transport);
      
      ScriptAction.passVariables( new Object[] { transportNode, name}, transportContext, onDeregister);
      onDeregister.run( transportContext);
    }
    return false;
  }

  @Override
  public void notifyError( ITransport transport, IContext context, Error error, IModelObject requestEnvelope)
  {
    IXAction onError = Conventions.getScript( document, context, onErrorExpr);
    if ( onError != null) 
    {
      StatefulContext messageContext = new StatefulContext( context);
      
      ModelObject transportNode = new ModelObject( "transport");
      transportNode.setValue( transport);
      
      ScriptAction.passVariables( new Object[] { transport, error.toString(), unwrap( transport, requestEnvelope)}, messageContext, onError);
      onError.run( messageContext);
    }
  }
  
  @Override
  public void notifyException( ITransport transport, IOException e)
  {
  }
  
  private IModelObject unwrap( ITransport transport, IModelObject envelope)
  {
    return transport.getProtocol().envelope().getMessage( envelope);
  }
  
  private String var;
  private IExpression bindHostExpr;
  private IExpression bindPortExpr;
  private IExpression protocolExpr;
  private IExpression schedulerExpr;
  private IExpression reliableExpr;
  private IExpression heartbeatPeriodExpr;
  private IExpression heartbeatTimeoutExpr;
  private IExpression onRegisterExpr;
  private IExpression onDeregisterExpr;
  private IExpression onReceiveExpr;
  private IExpression onErrorExpr;
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
}
