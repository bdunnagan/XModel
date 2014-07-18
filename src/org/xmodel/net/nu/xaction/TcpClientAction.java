package org.xmodel.net.nu.xaction;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.IEventHandler;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.PersistentTransport;
import org.xmodel.net.nu.ReliableTransport;
import org.xmodel.net.nu.tcp.TcpClientTransport;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

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
      TcpClientTransport tcpClient = new TcpClientTransport( ProtocolSchema.getProtocol( protocolExpr, context), context, scheduler);
      Conventions.putCache( context, var, tcpClient);
      
      if ( localHost != null) tcpClient.setLocalAddress( InetSocketAddress.createUnresolved( localHost, localPort));
      tcpClient.setRemoteAddress( new InetSocketAddress( remoteHost, remotePort));
      
      ITransportImpl transport = tcpClient;
      if ( retry) transport = new PersistentTransport( transport);
      if ( reliable) transport = new ReliableTransport( transport);
      
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
    public boolean notifyReceive( IModelObject message, IContext messageContext, IModelObject requestMessage)
    {
      // ignore acks
      if ( message == null) return false;
      
      IXAction onReceive = Conventions.getScript( document, messageContext, onReceiveExpr);
      if ( onReceive != null) 
      {
        Conventions.putCache( messageContext, "via", transport);
        onReceive.run( messageContext);
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
        Conventions.putCache( messageContext, "via", transport);
        messageContext.set( "error", error.toString());
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
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
  private IExpression onReceiveExpr;
  private IExpression onErrorExpr;
}
