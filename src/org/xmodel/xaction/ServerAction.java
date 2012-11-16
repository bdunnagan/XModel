package org.xmodel.xaction;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.xmodel.IModelObject;
import org.xmodel.net.XioServer;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that creates an XIO server.
 */
public class ServerAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    addressExpr = document.getExpression( "address", true);
    portExpr = document.getExpression( "port", true);

    
    
    IModelObject config = document.getRoot();
    Object when = config.removeAttribute( "when");
    script = document.createScript( "address", "port");
    if ( when != null) config.setAttribute( "when", when);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    String address = addressExpr.evaluateString( context);
    int port = (int)portExpr.evaluateNumber( context);
    
    XioServer server = new XioServer( context, context);
    Channel channel = server.start( address, port);
    channel.getPipeline().addLast( "3", new SetupChannelHandler( context));
    
    return null;
  }
  
  private final class SetupChannelHandler extends SimpleChannelDownstreamHandler
  {
    public SetupChannelHandler( IContext context)
    {
      this.context = context;
    }
    
    /* (non-Javadoc)
     * @see org.jboss.netty.channel.SimpleChannelDownstreamHandler#connectRequested(
     * org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void connectRequested( ChannelHandlerContext chc, ChannelStateEvent event) throws Exception
    {
      context.getModel().dispatch( connectRunnable);
    }

    /* (non-Javadoc)
     * @see org.jboss.netty.channel.SimpleChannelDownstreamHandler#disconnectRequested(
     * org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void disconnectRequested( ChannelHandlerContext chc, ChannelStateEvent event) throws Exception
    {
      context.getModel().dispatch( disconnectRunnable);
    }
    
    private Runnable connectRunnable = new Runnable() {
      public void run()
      {
        onConnect.run( context);
      }
    };
    
    private Runnable disconnectRunnable = new Runnable() {
      public void run()
      {
        onDisconnect.run( context);
      }
    };
    
    private IContext context;
  }
  
  private IExpression addressExpr;
  private IExpression portExpr;
  private IXAction onConnect;
  private IXAction onDisconnect;
}
