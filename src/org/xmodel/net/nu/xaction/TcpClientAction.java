package org.xmodel.net.nu.xaction;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.IConnectListener;
import org.xmodel.net.nu.IDisconnectListener;
import org.xmodel.net.nu.IErrorListener;
import org.xmodel.net.nu.IReceiveListener;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.PersistentTransport;
import org.xmodel.net.nu.ReliableTransport;
import org.xmodel.net.nu.TransportNotifier;
import org.xmodel.net.nu.tcp.TcpClientTransport;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class TcpClientAction extends GuardedAction implements IConnectListener, IDisconnectListener, IReceiveListener, IErrorListener
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
    onConnectExpr = document.getExpression( "onConnect", true);
    onDisconnectExpr = document.getExpression( "onDisconnect", true);
    onReceiveExpr = document.getExpression( "onReceive", true);
    onErrorExpr = document.getExpression( "onError", true);
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
    
    ScheduledExecutorService scheduler = (schedulerExpr != null)? (ScheduledExecutorService)Conventions.getCache( context, schedulerExpr): null;
    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1);
    
    try
    {
      TransportNotifier notifier = new TransportNotifier();
      notifier.addListener( (IConnectListener)this);
      notifier.addListener( (IDisconnectListener)this);
      notifier.addListener( (IReceiveListener)this);
      notifier.addListener( (IErrorListener)this);
      
      
      TcpClientTransport tcpClient = new TcpClientTransport( ProtocolSchema.getProtocol( protocolExpr, context), context, scheduler, notifier);
      Conventions.putCache( context, var, tcpClient);
      
      if ( localHost != null) tcpClient.setLocalAddress( InetSocketAddress.createUnresolved( localHost, localPort));
      tcpClient.setRemoteAddress( new InetSocketAddress( remoteHost, remotePort));
      
      ITransportImpl transport = tcpClient;
      if ( retry) transport = new PersistentTransport( transport);
      if ( reliable) transport = new ReliableTransport( transport);
      
      transport.connect( connectTimeout).await();
    }
    catch( Exception e)
    {
      throw new XActionException( e);
    }
    
    return null;
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

  @Override
  public void onReceive( ITransport transport, IModelObject message, IContext messageContext, IModelObject request) throws Exception
  {
    // ignore acks
    if ( message == null) return;
    
    IXAction onReceive = Conventions.getScript( document, messageContext, onReceiveExpr);
    if ( onReceive != null) 
    {
      Conventions.putCache( messageContext, "via", transport);
      onReceive.run( messageContext);
    }
  }
  
  @Override
  public void onError( ITransport transport, IContext context, ITransport.Error error, IModelObject request) throws Exception
  {
    IXAction onError = Conventions.getScript( document, context, onErrorExpr);
    if ( onError != null) 
    {
      IContext messageContext = new StatefulContext( context);
      Conventions.putCache( messageContext, "via", transport);
      messageContext.set( "error", error.toString());
      onError.run( messageContext);
    }
  }

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
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
  private IExpression onReceiveExpr;
  private IExpression onErrorExpr;
}
