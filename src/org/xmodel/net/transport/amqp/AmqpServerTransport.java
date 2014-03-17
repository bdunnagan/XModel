package org.xmodel.net.transport.amqp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLContext;

import org.xmodel.log.SLog;
import org.xmodel.net.IXioPeerRegistry;
import org.xmodel.net.IXioPeerRegistryListener;
import org.xmodel.net.XioPeer;
import org.xmodel.util.CountingThreadPoolExecutor;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ServerAction.IServerTransport;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;

public class AmqpServerTransport extends AmqpTransport implements IServerTransport
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.ServerAction.IServerTransport#listen(org.xmodel.xpath.expression.IContext, org.xmodel.xaction.IXAction, 
   * org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction)
   */
  @Override
  public IXioPeerRegistry listen( IContext context, IXAction onConnect, IXAction onDisconnect, IXAction onRegister, IXAction onUnregister) throws IOException
  {
    AmqpXioPeer peer = connect( context, null, onRegister, onUnregister);
    serverPeers.add( peer);
    return peer.getPeerRegistry();
  }

  /**
   * Connect to the AMQP server and return the XioPeer for the connection. 
   * @param context The context.
   * @param name The client subscription name.
   * @param onRegister Callback that is invoked when a peer registers.
   * @param onUnregister Callback that is invoked when a peer cancels registration.
   * @return Returns the peer.
   */
  protected AmqpXioPeer connect( final IContext context, String name, final IXAction onRegister, final IXAction onUnregister) throws IOException
  {
    int threads = (threadsExpr != null)? (int)threadsExpr.evaluateNumber( context): 0;
    boolean ssl = (sslExpr != null)? sslExpr.evaluateBoolean( context): false;
    int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): 5000;
   
    String queue = queueExpr.evaluateString( context);
    
    if ( ssl)
    {
      SSLContext sslContext = getSSLContext( context);
      connectionFactory.useSslProtocol( sslContext);
    }
    
    registry.addListener( new IXioPeerRegistryListener() {
      public void onRegister( final XioPeer peer, final String name)
      {
        SLog.infof( this, "Registering %s", peer.hashCode());
        context.getExecutor().execute( new Runnable() {
          public void run() 
          {
            StatefulContext eventContext = peer.getNetworkEventContext(); 
            if ( eventContext != null)
            {
              eventContext.set( "name", (name != null)? name: "");
              if ( onRegister != null) onRegister.run( eventContext);
            }
          }
        });
      }
      public void onUnregister( final XioPeer peer, final String name)
      {
        SLog.infof( this, "Un-registering %s", peer.hashCode());
        context.getExecutor().execute( new Runnable() {
          public void run() 
          {
            StatefulContext eventContext = peer.getNetworkEventContext(); 
            if ( eventContext != null)
            {
              eventContext.set( "name", (name != null)? name: "");
              if ( onUnregister != null) onUnregister.run( eventContext);
            }
          }
        });
      }
    });
    
    final ExecutorService ioExecutor = new CountingThreadPoolExecutor( queue, 0, threads, 300000);
    Address[] brokers = getBrokers( context);
    Connection connection = (brokers == null)?
        connectionFactory.newConnection( ioExecutor):
        connectionFactory.newConnection( ioExecutor, brokers);
    
    AmqpXioChannel serverChannel = new AmqpXioChannel( connection, AmqpQueueNames.getHeartbeatExchange(), ioExecutor, timeout);
    AmqpXioPeer serverPeer = new AmqpXioPeer( serverChannel, registry, context, context.getExecutor(), null, null);
    serverChannel.setPeer( serverPeer);

    // configure event notification context before starting consumer
    configureEventContext( serverPeer);
    serverChannel.startConsumer( queue, true, false);
    
    // start sending heartbeats from this server
//    serverChannel.setHeartbeatOutputQueue();
//    serverChannel.startHeartbeat( timeout / 2);
    
    return serverPeer;
  }

  /**
   * Set default variables of new event context.
   * @param context The event context.
   */
  private void configureEventContext( XioPeer peer)
  {
    StatefulContext context = peer.getNetworkEventContext();
    
    InetSocketAddress localAddress = peer.getLocalAddress();
    if ( localAddress != null) context.set( "localAddress", String.format( "%s:%d", localAddress.getAddress().getHostAddress(), localAddress.getPort()));
    
    InetSocketAddress remoteAddress = peer.getRemoteAddress();
    if ( remoteAddress != null) context.set( "remoteAddress", String.format( "%s:%d", remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort()));
  }
  
  
  // TODO: store peer in context
  private List<XioPeer> serverPeers = new ArrayList<XioPeer>();
}
