package org.xmodel.net.transport.amqp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

import org.xmodel.net.IXioPeerRegistry;
import org.xmodel.net.IXioPeerRegistryListener;
import org.xmodel.net.XioPeer;
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
    peers.add( peer);
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
    int threads = (threadsExpr != null)? (int)threadsExpr.evaluateNumber( context): 1;
    boolean ssl = (sslExpr != null)? sslExpr.evaluateBoolean( context): false;
   
    String queue = queueExpr.evaluateString( context);
    
    if ( ssl)
    {
      SSLContext sslContext = getSSLContext( context);
      connectionFactory.useSslProtocol( sslContext);
    }
    
    registry.addListener( new IXioPeerRegistryListener() {
      public void onRegister( final XioPeer peer, final String name)
      {
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
    
    final ExecutorService ioExecutor = (threads == 0)? Executors.newCachedThreadPool(): Executors.newFixedThreadPool( threads);
    Address[] brokers = getBrokers( context);
    Connection connection = (brokers == null)?
        connectionFactory.newConnection( ioExecutor):
        connectionFactory.newConnection( ioExecutor, brokers);
        
    AmqpXioChannel channel = new AmqpXioChannel( connection, "", ioExecutor);
    AmqpXioPeer peer = new AmqpXioPeer( channel, registry, context, context.getExecutor(), null, null);
    channel.setPeer( peer);
    channel.startConsumer( queue, true, false);
    return peer;
  }
  
  // TODO: store peer in context
  private List<XioPeer> peers = new ArrayList<XioPeer>();
}