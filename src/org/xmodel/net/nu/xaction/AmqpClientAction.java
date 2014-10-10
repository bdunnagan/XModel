package org.xmodel.net.nu.xaction;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.xmodel.log.Log;
import org.xmodel.net.nu.EventPipe;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.algo.HeartbeatAlgo;
import org.xmodel.net.nu.algo.ReconnectAlgo;
import org.xmodel.net.nu.algo.ReliableAlgo;
import org.xmodel.net.nu.algo.RequestTrackingAlgo;
import org.xmodel.net.nu.amqp.AmqpTransport;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

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
    purgeExpr = document.getExpression( "purge", true);
    reliableExpr = document.getExpression( "reliable", true);
    heartbeatPeriodExpr = document.getExpression( "heartbeatPeriod", true);
    heartbeatTimeoutExpr = document.getExpression( "heartbeatTimeout", true);
  }
  
  @Override
  protected Object[] doAction( IContext context)
  {
    String amqpHost = (amqpHostExpr != null)? amqpHostExpr.evaluateString( context): null;
    int amqpPort = (amqpPortExpr != null)? (int)amqpPortExpr.evaluateNumber( context): 5672;
    
    int connectTimeout = (connectTimeoutExpr != null)? (int)connectTimeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
    boolean retry = (retryExpr != null)? retryExpr.evaluateBoolean( context): true;
    boolean purge = (purgeExpr != null)? purgeExpr.evaluateBoolean( context): false;
    boolean reliable = (reliableExpr != null)? reliableExpr.evaluateBoolean( context): true;
    
    String publishExchange = (publishExchangeExpr != null)? publishExchangeExpr.evaluateString( context): null;
    String publishQueue = (publishQueueExpr != null)? publishQueueExpr.evaluateString( context): null;
    String consumeQueue = (consumeQueueExpr != null)? consumeQueueExpr.evaluateString( context): null;
    
    ScheduledExecutorService scheduler = (schedulerExpr != null)? (ScheduledExecutorService)Conventions.getCache( context, schedulerExpr): null;
    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1);
    
    try
    {
      AmqpTransport amqpClient = new AmqpTransport( ProtocolSchema.getProtocol( protocolExpr, context), context);
      Conventions.putCache( context, var, amqpClient);
            
      amqpClient.setRemoteAddress( new InetSocketAddress( amqpHost, amqpPort));
      if ( publishExchange != null) amqpClient.setPublishExchange( publishExchange);
      if ( publishQueue != null) amqpClient.setPublishQueue( publishQueue);
      if ( consumeQueue != null) amqpClient.setConsumeQueue( consumeQueue, purge);
      
      ITransportImpl transport = amqpClient;
      EventPipe eventPipe = transport.getEventPipe();
      eventPipe.addFirst( new RequestTrackingAlgo( transport, scheduler));
      if ( retry) eventPipe.addLast( new ReconnectAlgo( transport, scheduler));
      if ( reliable) eventPipe.addLast( new ReliableAlgo( transport, scheduler));
      
      int heartbeatPeriod = (heartbeatPeriodExpr != null)? (int)heartbeatPeriodExpr.evaluateNumber( context): 10000;
      int heartbeatTimeout = (heartbeatTimeoutExpr != null)? (int)heartbeatTimeoutExpr.evaluateNumber( context): 30000;
      if ( heartbeatPeriod > 0) eventPipe.addFirst( new HeartbeatAlgo( transport, heartbeatPeriod, heartbeatTimeout, scheduler));
      
      eventPipe.addLast( new EventHandlerAdapter( getDocument(), transport, context));
      
      transport.setConnectTimeout( connectTimeout);
      transport.connect().await();
    }
    catch( Exception e)
    {
      throw new XActionException( e);
    }
    
    return null;
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
  private IExpression purgeExpr;
  private IExpression reliableExpr;
  private IExpression heartbeatPeriodExpr;
  private IExpression heartbeatTimeoutExpr;
}
