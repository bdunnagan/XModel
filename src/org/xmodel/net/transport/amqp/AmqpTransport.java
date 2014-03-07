package org.xmodel.net.transport.amqp;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.net.IXioPeerRegistry;
import org.xmodel.net.IXioPeerRegistryListener;
import org.xmodel.net.MemoryXioPeerRegistry;
import org.xmodel.net.OpenTrustStore;
import org.xmodel.net.XioPeer;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class AmqpTransport
{
  public enum Role { client, server};
  
  /**
   * Create an AMQP transport with the specified role.  Two queues are created in either role, a request
   * queue and a response queue.  A client will send requests to the request queue, while a server will
   * consume requests from the request queue.  Similarly, a server will send responses to the response
   * queue and a client consumes responses from the response queue. 
   * @param role The role.
   */
  public AmqpTransport( Role role)
  {
    this.role = role;
    registry = new MemoryXioPeerRegistry();
  }

  /**
   * Configure the transport.
   * @param config The configuration element.
   */
  public void configure( IModelObject config)
  {
    sslExpr = Xlate.get( config, "ssl", Xlate.childGet( config, "ssl", (IExpression)null));
    threadsExpr = Xlate.get( config, "threads", Xlate.childGet( config, "threads", (IExpression)null));
    brokersExpr = Xlate.get( config, "brokers", Xlate.childGet( config, "brokers", (IExpression)null));
    queueExpr = Xlate.get( config, "queue", Xlate.childGet( config, "queue", (IExpression)null));
    
    connectionFactory = new ConnectionFactory();
    connectionFactory.setHost( "localhost");
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
        AmqpXioChannel channel = (AmqpXioChannel)peer.getChannel();
        channel.setReplyQueue( name);
        
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
        AmqpXioChannel channel = (AmqpXioChannel)peer.getChannel();
        channel.setReplyQueue( null);
        
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
    
    ExecutorService ioExecutor = (threads == 0)? Executors.newCachedThreadPool(): Executors.newFixedThreadPool( threads);
    Address[] brokers = getBrokers( context);
    Connection connection = (brokers == null)?
        connectionFactory.newConnection( ioExecutor):
        connectionFactory.newConnection( ioExecutor, brokers);
        
    String inQueue, outQueue;
    switch( role)
    {
      case server:
        inQueue = queue;
        outQueue = null;
        break;
        
      case client:
        inQueue = name;
        outQueue = queue;
        break;
        
      default:
        inQueue = outQueue = null;
        break;
    }
        
    AmqpXioChannel channel = new AmqpXioChannel( connection, "", inQueue, outQueue);
    AmqpXioPeer peer = new AmqpXioPeer( channel, registry, context, context.getExecutor(), null, null);
    channel.setPeer( peer);
    
    channel.startConsumer();
    
    return peer;
  }
  
  /**
   * @return Returns null or the array of brokers that will be tried.
   * @param context The context.
   */
  private Address[] getBrokers( IContext context)
  {
    if ( brokersExpr == null) return null;
    
    List<Address> addresses = new ArrayList<Address>();
    
    for( IModelObject broker: brokersExpr.query( context, null))
    {
      String host = Xlate.get( broker, "host", Xlate.childGet( broker, "host", ""));
      int port = Xlate.get( broker, "port", Xlate.childGet( broker, "port", 0));
      if ( host.length() > 0)
      {
        addresses.add( (port == 0)? new Address( host): new Address( host, port));
      }
    }
    
    return addresses.toArray( new Address[ 0]);
  }
  
  /**
   * Get the SSLContext if ssl is requested.
   * @param context The context.
   * @return Returns null or the SSLContext.
   */
  private SSLContext getSSLContext( IContext context)
  {
    boolean ssl = (sslExpr != null)? sslExpr.evaluateBoolean( context): false; 
    if ( !ssl) return null;
    
    try
    {
      SSLContext sslContext = SSLContext.getInstance( "TLS");
      sslContext.init( null, new TrustManager[] { new OpenTrustStore()}, new SecureRandom());
      return sslContext;
    }
    catch( Exception e)
    {
      throw new XActionException( e);
    }
  }
  
  //private static Log log = Log.getLog( AmqpTransport.class);
  
  protected Role role;
  protected ConnectionFactory connectionFactory;
  protected IExpression queueExpr;
  protected IExpression threadsExpr;
  protected IExpression sslExpr;
  protected IExpression brokersExpr;
  protected IXioPeerRegistry registry;
}
