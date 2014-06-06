package org.xmodel.net.nu.xaction;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.IConnectListener;
import org.xmodel.net.nu.IDisconnectListener;
import org.xmodel.net.nu.IReceiveListener;
import org.xmodel.net.nu.ITimeoutListener;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.tcp.TcpClientTransport;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class TcpClientAction extends GuardedAction implements IReceiveListener, ITimeoutListener, IConnectListener, IDisconnectListener
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
    onReceiveExpr = document.getExpression( "onReceive", true);
    onTimeoutExpr = document.getExpression( "onTimeout", true);
    onConnectExpr = document.getExpression( "onConnect", true);
    onDisconnectExpr = document.getExpression( "onDisconnect", true);
  }
  
  @Override
  protected Object[] doAction( IContext context)
  {
    String localHost = (localHostExpr != null)? localHostExpr.evaluateString( context): null;
    int localPort = (localPortExpr != null)? (int)localPortExpr.evaluateNumber( context): 0;
    
    String remoteHost = remoteHostExpr.evaluateString( context);
    int remotePort = (int)remotePortExpr.evaluateNumber( context);
    
    int connectTimeout = (connectTimeoutExpr != null)? (int)connectTimeoutExpr.evaluateNumber( context): Integer.MAX_VALUE;
    
    ScheduledExecutorService scheduler = (schedulerExpr != null)? (ScheduledExecutorService)Conventions.getCache( context, schedulerExpr): null;
    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1);

    try
    {
      TcpClientTransport transport = new TcpClientTransport( ProtocolSchema.getProtocol( protocolExpr, context), context, scheduler, null, null,
          Collections.singletonList( (IConnectListener)this), Collections.singletonList( (IDisconnectListener)this));
      
      Conventions.putCache( context, var, transport);
      
      if ( localHost != null) transport.setLocalAddress( InetSocketAddress.createUnresolved( localHost, localPort));
      transport.setRemoteAddress( new InetSocketAddress( remoteHost, remotePort));
      
      transport.connect( connectTimeout).await();
    }
    catch( Exception e)
    {
      throw new XActionException( e);
    }
    
    return null;
  }
  
  @Override
  public void onTimeout( ITransport transport, IModelObject message, IContext context) throws Exception
  {
    IXAction onTimeout = Conventions.getScript( document, context, onTimeoutExpr);
    if ( onTimeout != null) 
    {
      IContext messageContext = new StatefulContext( context);
      Conventions.putCache( messageContext, "via", transport);
      messageContext.set( "message", message);
      onTimeout.run( messageContext);
    }
  }

  @Override
  public void onReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject request) throws Exception
  {
    IXAction onReceive = Conventions.getScript( document, messageContext, onReceiveExpr);
    if ( onReceive != null) 
    {
      Conventions.putCache( messageContext, "via", transport);
      messageContext.set( "message", message);
      onReceive.run( messageContext);
    }
  }
  
  @Override
  public void onConnect( ITransport transport, IContext context)
  {
    IXAction onConnect = Conventions.getScript( document, context, onConnectExpr);
    if ( onConnect != null) onConnect.run( context);
  }

  @Override
  public void onDisconnect( ITransport transport, IContext context)
  {
    IXAction onDisconnect = Conventions.getScript( document, context, onDisconnectExpr);
    if ( onDisconnect != null) onDisconnect.run( context);
  }

  private String var;
  private IExpression localHostExpr;
  private IExpression localPortExpr;
  private IExpression remoteHostExpr;
  private IExpression remotePortExpr;
  private IExpression connectTimeoutExpr;
  private IExpression protocolExpr;
  private IExpression schedulerExpr;
  private IExpression onReceiveExpr;
  private IExpression onTimeoutExpr;
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
}
