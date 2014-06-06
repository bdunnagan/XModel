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
import org.xmodel.net.nu.tcp.TcpServerRouter;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class TcpServerAction extends GuardedAction implements IConnectListener, IDisconnectListener, IReceiveListener, IErrorListener
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
    onReceiveExpr = document.getExpression( "onReceive", true);
    onTimeoutExpr = document.getExpression( "onTimeout", true);
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

    try
    {
      TcpServerRouter server = new TcpServerRouter( ProtocolSchema.getProtocol( protocolExpr, context), context, scheduler);
      Conventions.putCache( context, var, server);
      
      server.addListener( (IConnectListener)this);
      server.addListener( (IDisconnectListener)this);
      server.addListener( (IReceiveListener)this);
      server.addListener( (IErrorListener)this);
      
      server.start( new InetSocketAddress( bindHost, bindPort));
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
    IXAction onReceive = Conventions.getScript( document, messageContext, onReceiveExpr);
    if ( onReceive != null) 
    {
      Conventions.putCache( messageContext, "via", transport);
      messageContext.set( "message", message);
      onReceive.run( messageContext);
    }
  }
  
  @Override
  public void onError( ITransport transport, IContext context, ITransport.Error error) throws Exception
  {
    IXAction onTimeout = Conventions.getScript( document, context, onTimeoutExpr);
    if ( onTimeout != null) 
    {
      IContext messageContext = new StatefulContext( context);
      messageContext.set( "error", error.toString());
      onTimeout.run( messageContext);
    }
  }

  private String var;
  private IExpression bindHostExpr;
  private IExpression bindPortExpr;
  private IExpression protocolExpr;
  private IExpression schedulerExpr;
  private IExpression onReceiveExpr;
  private IExpression onTimeoutExpr;
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
}
