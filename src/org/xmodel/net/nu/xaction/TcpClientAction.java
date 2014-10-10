package org.xmodel.net.nu.xaction;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.xmodel.log.Log;
import org.xmodel.net.nu.EventPipe;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.algo.ExpirationAlgo;
import org.xmodel.net.nu.algo.HeartbeatAlgo;
import org.xmodel.net.nu.algo.ReconnectAlgo;
import org.xmodel.net.nu.algo.RegisterAlgo;
import org.xmodel.net.nu.algo.ReliableAlgo;
import org.xmodel.net.nu.algo.RequestTrackingAlgo;
import org.xmodel.net.nu.tcp.TcpClientTransport;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class TcpClientAction extends GuardedAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    var = Conventions.getVarName( document.getRoot(), true);
    localHostExpr = document.getExpression( "localHost", true);
    localPortExpr = document.getExpression( "localPort", true);
    remoteHostExpr = document.getExpression( "remoteHost", true);
    remotePortExpr = document.getExpression( "remotePort", true);
    connectTimeoutExpr = document.getExpression( "connectTimeout", true);
    protocolExpr = document.getExpression( "protocol", true);
    schedulerExpr = document.getExpression( "scheduler", true);
    retryExpr = document.getExpression( "retry", true);
    reliableExpr = document.getExpression( "reliable", true);
    heartbeatPeriodExpr = document.getExpression( "heartbeatPeriod", true);
    heartbeatTimeoutExpr = document.getExpression( "heartbeatTimeout", true);
  }
  
  @Override
  protected Object[] doAction( IContext context)
  {
    String localHost = (localHostExpr != null)? localHostExpr.evaluateString( context): null;
    int localPort = (localPortExpr != null)? (int)localPortExpr.evaluateNumber( context): 0;
    
    String remoteHost = remoteHostExpr.evaluateString( context);
    int remotePort = (int)remotePortExpr.evaluateNumber( context);
    
    int connectTimeout = (connectTimeoutExpr != null)? (int)connectTimeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
    boolean retry = (retryExpr != null)? retryExpr.evaluateBoolean( context): true;
    boolean reliable = (reliableExpr != null)? reliableExpr.evaluateBoolean( context): true;
    
    int heartbeatPeriod = (heartbeatPeriodExpr != null)? (int)heartbeatPeriodExpr.evaluateNumber( context): 10000;
    int heartbeatTimeout = (heartbeatTimeoutExpr != null)? (int)heartbeatTimeoutExpr.evaluateNumber( context): 30000;
    
    ScheduledExecutorService scheduler = (schedulerExpr != null)? (ScheduledExecutorService)Conventions.getCache( context, schedulerExpr): null;
    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1);
    
    try
    {
      TcpClientTransport tcpClient = new TcpClientTransport( ProtocolSchema.getProtocol( protocolExpr, context), context);
      
      if ( localHost != null) tcpClient.setLocalAddress( InetSocketAddress.createUnresolved( localHost, localPort));
      tcpClient.setRemoteAddress( new InetSocketAddress( remoteHost, remotePort));
      
      ITransportImpl transport = tcpClient;
      
      EventPipe eventPipe = transport.getEventPipe();
      eventPipe.addFirst( new RequestTrackingAlgo( transport, scheduler));
      eventPipe.addFirst( new RegisterAlgo( transport, tcpClient));
      eventPipe.addFirst( new ExpirationAlgo( transport));
      if ( retry) eventPipe.addLast( new ReconnectAlgo( transport, scheduler));
      if ( reliable) eventPipe.addLast( new ReliableAlgo( transport, scheduler));
      if ( heartbeatPeriod > 0) eventPipe.addFirst( new HeartbeatAlgo( transport, heartbeatPeriod, heartbeatTimeout, scheduler));
      eventPipe.addLast( new EventHandlerAdapter( getDocument(), transport, context));
      
      transport.setConnectTimeout( connectTimeout);
      transport.connect().await();
      
      Conventions.putCache( context, var, transport);
    }
    catch( Exception e)
    {
      throw new XActionException( e);
    }
    
    return null;
  }
  
  public static Log log = Log.getLog( TcpClientAction.class);

  private String var;
  private IExpression localHostExpr;
  private IExpression localPortExpr;
  private IExpression remoteHostExpr;
  private IExpression remotePortExpr;
  private IExpression connectTimeoutExpr;
  private IExpression protocolExpr;
  private IExpression schedulerExpr;
  private IExpression retryExpr;
  private IExpression reliableExpr;
  private IExpression heartbeatPeriodExpr;
  private IExpression heartbeatTimeoutExpr;
}

