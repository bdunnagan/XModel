package org.xmodel.net.transport.amqp;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLContext;

import org.xmodel.net.IXioPeerRegistryListener;
import org.xmodel.net.XioPeer;
import org.xmodel.util.CountingThreadPoolExecutor;
import org.xmodel.xaction.ClientAction.IClientTransport;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.IXAction;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;

public class AmqpClientTransport extends AmqpTransport implements IClientTransport
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.ClientAction.IClientTransport#connect(org.xmodel.xpath.expression.IContext, java.lang.String, 
   * org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction)
   */
  @Override
  public XioPeer connect( 
      final IContext context, 
      final String name, 
      final IXAction onConnect, 
      final IXAction onDisconnect, 
      final IXAction onError, 
      final IXAction onRegister, 
      final IXAction onUnregister) throws IOException
  {
    int threads = (threadsExpr != null)? (int)threadsExpr.evaluateNumber( context): 0;
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
    
    final ExecutorService ioExecutor = new CountingThreadPoolExecutor( queue, 0, threads, 300000);
    Address[] brokers = getBrokers( context);
    Connection connection = (brokers == null)?
        connectionFactory.newConnection( ioExecutor):
        connectionFactory.newConnection( ioExecutor, brokers);

    AmqpXioChannel initChannel = new AmqpXioChannel( connection, "", ioExecutor);
    final AmqpXioPeer peer = new AmqpXioPeer( initChannel, registry, context, context.getExecutor(), null, null);
    initChannel.setPeer( peer);
    initChannel.declareOutputQueue( queue, true, false);
    
    AmqpXioChannel subscribeChannel = new AmqpXioChannel( connection, "", ioExecutor);
    subscribeChannel.setPeer( peer);
    peer.setSubscribeChannel( subscribeChannel);
    
    subscribeChannel.declareOutputQueue( AmqpQueueNames.getInputQueue( name), false, true);
    subscribeChannel.startConsumer( AmqpQueueNames.getOutputQueue( queue, name), false, true);

    subscribeChannel.startHeartbeat( 9000, true);

    if ( onConnect != null) 
    {
      ioExecutor.execute( new Runnable() {
        public void run() 
        {
          StatefulContext eventContext = peer.getNetworkEventContext();
          Conventions.putCache( eventContext, "peer", peer);
          onConnect.run( eventContext);
        }
      });
    }
    
    return peer;
  }
}
