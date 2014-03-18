package org.xmodel.net.transport.amqp;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import javax.net.ssl.SSLContext;
import org.xmodel.future.AsyncFuture;
import org.xmodel.log.SLog;
import org.xmodel.net.IXioChannel;
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
    int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): 10000;
   
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
    
    final ExecutorService ioExecutor = new CountingThreadPoolExecutor( queue, 0, threads, 300);
    Address[] brokers = getBrokers( context);
    Connection connection = (brokers == null)?
        connectionFactory.newConnection( ioExecutor):
        connectionFactory.newConnection( ioExecutor, brokers);

    AmqpXioChannel initChannel = new AmqpXioChannel( connection, "", ioExecutor, 0);
    final AmqpXioPeer peer = new AmqpXioPeer( initChannel, registry, context, context.getExecutor(), null, null);
    initChannel.setPeer( peer);
    initChannel.setOutputQueue( queue, true, false);
    
    AmqpXioChannel subscribeChannel = new AmqpXioChannel( connection, "", ioExecutor, timeout);
    subscribeChannel.setPeer( peer);
    peer.setSubscribeChannel( subscribeChannel);
    subscribeChannel.setOutputQueue( AmqpQueueNames.getInputQueue( name), false, true);
    subscribeChannel.startConsumer( AmqpQueueNames.getOutputQueue( AmqpQualifiedNames.createQualifiedName( name)), false, true);

    subscribeChannel.getCloseFuture().addListener( new AsyncFuture.IListener<IXioChannel>() {
      public void notifyComplete( AsyncFuture<IXioChannel> future) throws Exception
      {
        try
        {
          ((AmqpXioChannel)future.getInitiator()).getConnection().close();
        }
        catch( Exception e)
        {
          SLog.exception( this, e);
        }
      }
    });
    
    class TimeoutTask implements AsyncFuture.IListener<AmqpXioPeer>
    {
      public TimeoutTask( AmqpXioPeer peer) { this.peer = peer;}
      public void notifyComplete( AsyncFuture<AmqpXioPeer> future) throws Exception 
      {
        AmqpXioChannel channel = (AmqpXioChannel)peer.getChannel();
        peer.reregister();
        channel.startHeartbeatTimeout().addListener( this);
      }
      private AmqpXioPeer peer;
    }
    
    subscribeChannel.startHeartbeat();
    subscribeChannel.startHeartbeatTimeout().addListener( new TimeoutTask( peer));

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
