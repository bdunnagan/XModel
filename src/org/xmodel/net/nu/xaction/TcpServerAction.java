package org.xmodel.net.nu.xaction;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.IEventHandler;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.tcp.TcpServerRouter;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class TcpServerAction extends GuardedAction
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

    boolean reliable = (reliableExpr != null)? reliableExpr.evaluateBoolean( context): true;
    
    try
    {
      TcpServerRouter server = new TcpServerRouter( ProtocolSchema.getProtocol( protocolExpr, context), context, scheduler, reliable);
      Conventions.putCache( context, var, server);
 
      server.setEventHandler( new EventHandler( context));
      server.start( new InetSocketAddress( bindHost, bindPort));
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
    public boolean notifyConnect() throws IOException
    {
      IXAction onConnect = Conventions.getScript( document, context, onConnectExpr);
      if ( onConnect != null) onConnect.run( context);
      return false;
    }
    
    @Override
    public boolean notifyDisconnect() throws IOException
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
        messageContext.set( "message", message);
        onReceive.run( messageContext);
      }
      
      return false;
    }
    
    @Override
    public boolean notifyError( IContext context, Error error, IModelObject request)
    {
      IXAction onTimeout = Conventions.getScript( document, context, onTimeoutExpr);
      if ( onTimeout != null) 
      {
        IContext messageContext = new StatefulContext( context);
        messageContext.set( "error", error.toString());
        onTimeout.run( messageContext);
      }
      
      return false;
    }
    
    @Override
    public boolean notifyException( IOException e)
    {
      // TODO Auto-generated method stub
      return false;
    }

    private ITransport transport;
    private IContext context;    
  }
  
  private String var;
  private IExpression bindHostExpr;
  private IExpression bindPortExpr;
  private IExpression protocolExpr;
  private IExpression schedulerExpr;
  private IExpression reliableExpr;
  private IExpression onReceiveExpr;
  private IExpression onTimeoutExpr;
  private IExpression onConnectExpr;
  private IExpression onDisconnectExpr;
}
