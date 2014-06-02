package org.xmodel.net.nu.xaction;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.net.nu.IConnectListener;
import org.xmodel.net.nu.IDisconnectListener;
import org.xmodel.net.nu.IProtocol;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.tcp.TcpServerRouter;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class TcpServerAction extends GuardedAction implements IConnectListener, IDisconnectListener
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
    onConnectExpr = document.getExpression( "onConnect", true);
    onDisconnectExpr = document.getExpression( "onDisconnect", true);
  }
  
  @Override
  protected Object[] doAction( IContext context)
  {
    String bindHost = (bindHostExpr != null)? bindHostExpr.evaluateString( context): null;
    int bindPort = (bindPortExpr != null)? (int)bindPortExpr.evaluateNumber( context): 0;
        
    ScheduledExecutorService scheduler = (ScheduledExecutorService)Conventions.getCache( context, schedulerExpr);
    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1);

    try
    {
      TcpServerRouter server = new TcpServerRouter( getProtocol( context), context, scheduler);
      Conventions.putCache( context, var, server);
      
      server.addListener( (IConnectListener)this);
      server.addListener( (IDisconnectListener)this);
      
      server.start( InetSocketAddress.createUnresolved( bindHost, bindPort));
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
  private IExpression bindHostExpr;
  private IExpression bindPortExpr;
  private IExpression protocolExpr;
  private IExpression schedulerExpr;
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
}
