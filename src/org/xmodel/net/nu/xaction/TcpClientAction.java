package org.xmodel.net.nu.xaction;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.net.nu.IConnectListener;
import org.xmodel.net.nu.IDisconnectListener;
import org.xmodel.net.nu.IProtocol;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.tcp.TcpClientTransport;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class TcpClientAction extends GuardedAction implements IConnectListener, IDisconnectListener
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
    
    int connectTimeout = (connectTimeoutExpr != null)? (int)connectTimeoutExpr.evaluateNumber( context): 0;
    
    ScheduledExecutorService scheduler = (ScheduledExecutorService)Conventions.getCache( context, schedulerExpr);
    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1);

    try
    {
      TcpClientTransport transport = new TcpClientTransport( getProtocol( context), context, scheduler);
      Conventions.putCache( context, var, transport);
      
      if ( localHost != null) transport.setLocalAddress( InetSocketAddress.createUnresolved( localHost, localPort));
      transport.setRemoteAddress( InetSocketAddress.createUnresolved( remoteHost, remotePort));
      
      transport.addListener( (IConnectListener)this);
      transport.addListener( (IDisconnectListener)this);
      transport.connect( connectTimeout).await();
    }
    catch( Exception e)
    {
      throw new XActionException( e);
    }
    
    return null;
  }
  
  private IProtocol getProtocol( IContext context)
  {
    String protocol = (protocolExpr != null)? protocolExpr.evaluateString( context): "xml";
    protocol = protocol.substring( 0, 1).toUpperCase() + protocol.substring( 1);
    String className = "org.xmodel.net.nu." + protocol + "Protocol";
    try
    {
      Class<?> clss = getClass().getClassLoader().loadClass( className);
      return (IProtocol)clss.newInstance();
    }
    catch( Exception e)
    {
      throw new XActionException( "Unrecognized protocol.", e);
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
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
}
