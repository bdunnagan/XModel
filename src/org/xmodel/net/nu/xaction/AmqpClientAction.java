package org.xmodel.net.nu.xaction;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.IEventHandler;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.Heartbeater;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.PersistentTransport;
import org.xmodel.net.nu.ReliableTransport;
import org.xmodel.net.nu.amqp.AmqpTransport;
import org.xmodel.net.nu.tcp.TcpChildTransport;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class AmqpClientAction extends GuardedAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    var = Conventions.getVarName( document.getRoot(), true);
    amqpHostExpr = document.getExpression( "amqpHost", true);
    amqpPortExpr = document.getExpression( "amqpPort", true);
    publishExchangeExpr = document.getExpression( "publishExchange", true);
    publishQueueExpr = document.getExpression( "publishQueue", true);
    consumeQueueExpr = document.getExpression( "consumeQueue", true);
    connectTimeoutExpr = document.getExpression( "connectTimeout", true);
    protocolExpr = document.getExpression( "protocol", true);
    schedulerExpr = document.getExpression( "scheduler", true);
    retryExpr = document.getExpression( "retry", true);
    reliableExpr = document.getExpression( "reliable", true);
    heartbeatPeriodExpr = document.getExpression( "heartbeatPeriod", true);
    heartbeatTimeoutExpr = document.getExpression( "heartbeatTimeout", true);
    onConnectExpr = document.getExpression( "onConnect", true);
    onDisconnectExpr = document.getExpression( "onDisconnect", true);
    onRegisterExpr = document.getExpression( "onRegister", true);
    onDeregisterExpr = document.getExpression( "onDeregister", true);
    onReceiveExpr = document.getExpression( "onReceive", true);
    onErrorExpr = document.getExpression( "onError", true);
  }
  
  @Override
  protected Object[] doAction( IContext context)
  {
    String amqpHost = (amqpHostExpr != null)? amqpHostExpr.evaluateString( context): null;
    int amqpPort = (amqpPortExpr != null)? (int)amqpPortExpr.evaluateNumber( context): 5672;
    
    int connectTimeout = (connectTimeoutExpr != null)? (int)connectTimeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
    boolean retry = (retryExpr != null)? retryExpr.evaluateBoolean( context): true;
    boolean reliable = (reliableExpr != null)? reliableExpr.evaluateBoolean( context): true;
    
    String publishExchange = (publishExchangeExpr != null)? publishExchangeExpr.evaluateString( context): null;
    String publishQueue = (publishQueueExpr != null)? publishQueueExpr.evaluateString( context): null;
    String consumeQueue = (consumeQueueExpr != null)? consumeQueueExpr.evaluateString( context): null;
    
    ScheduledExecutorService scheduler = (schedulerExpr != null)? (ScheduledExecutorService)Conventions.getCache( context, schedulerExpr): null;
    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1);
    
    try
    {
      AmqpTransport amqpClient = new AmqpTransport( ProtocolSchema.getProtocol( protocolExpr, context), context, scheduler);
      Conventions.putCache( context, var, amqpClient);
            
      amqpClient.setRemoteAddress( new InetSocketAddress( amqpHost, amqpPort));
      if ( publishExchange != null) amqpClient.setPublishExchange( publishExchange);
      if ( publishQueue != null) amqpClient.setPublishQueue( publishQueue);
      if ( consumeQueue != null) amqpClient.setConsumeQueue( consumeQueue);
      
      ITransportImpl transport = amqpClient;
      if ( retry) transport = new PersistentTransport( transport);
      if ( reliable) transport = new ReliableTransport( transport);
      
      int heartbeatPeriod = (heartbeatPeriodExpr != null)? (int)heartbeatPeriodExpr.evaluateNumber( context): 10000;
      int heartbeatTimeout = (heartbeatTimeoutExpr != null)? (int)heartbeatTimeoutExpr.evaluateNumber( context): 30000;
      if ( heartbeatPeriod > 0) transport.getEventPipe().addFirst( new Heartbeater( ((TcpChildTransport)transport), heartbeatPeriod, heartbeatTimeout));
      
      transport.getEventPipe().addLast( new EventHandler( transport, context));
      
      transport.connect( connectTimeout).await();
    }
    catch( Exception e)
    {
      throw new XActionException( e);
    }
    
    return null;
  }
  
  class EventHandler implements IEventHandler
  {
    public EventHandler( ITransport transport, IContext context)
    {
      this.transport = transport;
      this.context = context;
    }
    
    @Override
    public boolean notifyConnect(IContext transportContext) throws IOException
    {
      IXAction onConnect = Conventions.getScript( document, context, onConnectExpr);
      if ( onConnect != null) onConnect.run( context);
      
      return false;
    }
  
    @Override
    public boolean notifyDisconnect(IContext transportContext) throws IOException
    {
      IXAction onDisconnect = Conventions.getScript( document, context, onDisconnectExpr);
      if ( onDisconnect != null) onDisconnect.run( context);
      return false;
    }
  
    @Override
    public boolean notifyReceive( ByteBuffer buffer) throws IOException
    {
      return false;
    }
  
    @Override
    public boolean notifyReceive( IModelObject envelope)
    {
      return false;
    }

    @Override
    public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage)
    {
      // ignore acks
      if ( message == null) return false;
      
      IXAction onReceive = Conventions.getScript( document, messageContext, onReceiveExpr);
      if ( onReceive != null) 
      {
        ModelObject transportNode = new ModelObject( "transport");
        transportNode.setValue( transport);
        
        ScriptAction.passVariables( new Object[] { transportNode, message}, messageContext, onReceive);
        onReceive.run( messageContext);
      }
      
      return false;
    }
  
    @Override
    public boolean notifyRegister( IContext transportContext, String name)
    {
      IXAction onRegister = Conventions.getScript( document, transportContext, onRegisterExpr);
      if ( onRegister != null) 
      {
        ModelObject transportNode = new ModelObject( "transport");
        transportNode.setValue( transport);
        
        ScriptAction.passVariables( new Object[] { transportNode}, transportContext, onRegister);
        onRegister.run( transportContext);
      }
      return false;
    }

    @Override
    public boolean notifyDeregister( IContext transportContext, String name)
    {
      IXAction onDeregister = Conventions.getScript( document, transportContext, onDeregisterExpr);
      if ( onDeregister != null) 
      {
        ModelObject transportNode = new ModelObject( "transport");
        transportNode.setValue( transport);
        
        ScriptAction.passVariables( new Object[] { transportNode}, transportContext, onDeregister);
        onDeregister.run( transportContext);
      }
      return false;
    }

    @Override
    public boolean notifyError( IContext context, Error error, IModelObject request)
    {
      IXAction onError = Conventions.getScript( document, context, onErrorExpr);
      if ( onError != null) 
      {
        IContext messageContext = new StatefulContext( context);
        
        ModelObject transportNode = new ModelObject( "transport");
        transportNode.setValue( transport);
        
        ScriptAction.passVariables( new Object[] { transportNode, error.toString()}, messageContext, onError);
        onError.run( messageContext);
      }
      
      return false;
    }
  
    @Override
    public boolean notifyException( IOException e)
    {
      log.exception( e);
      return false;
    }
    
    private ITransport transport;
    private IContext context;    
  }
  
  public static Log log = Log.getLog( AmqpClientAction.class);

  private String var;
  private IExpression amqpHostExpr;
  private IExpression amqpPortExpr;
  private IExpression publishExchangeExpr;
  private IExpression publishQueueExpr;
  private IExpression consumeQueueExpr;
  private IExpression connectTimeoutExpr;
  private IExpression protocolExpr;
  private IExpression schedulerExpr;
  private IExpression retryExpr;
  private IExpression reliableExpr;
  private IExpression heartbeatPeriodExpr;
  private IExpression heartbeatTimeoutExpr;
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
  private IExpression onRegisterExpr;
  private IExpression onDeregisterExpr;
  private IExpression onReceiveExpr;
  private IExpression onErrorExpr;
}
