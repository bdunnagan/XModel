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
import org.xmodel.log.Log;
import org.xmodel.net.IXioPeerRegistry;
import org.xmodel.net.IXioPeerRegistryListener;
import org.xmodel.net.MemoryXioPeerRegistry;
import org.xmodel.net.OpenTrustStore;
import org.xmodel.net.XioPeer;
import org.xmodel.xaction.ClientAction.IClientTransport;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class AmqpClientTransport implements IClientTransport
{
  public AmqpClientTransport()
  {
    registry = new MemoryXioPeerRegistry();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.ClientAction.IClientTransport#configure(org.xmodel.IModelObject)
   */
  @Override
  public void configure( IModelObject config)
  {
    sslExpr = Xlate.get( config, "ssl", Xlate.childGet( config, "ssl", (IExpression)null));
    threadsExpr = Xlate.get( config, "threads", Xlate.childGet( config, "threads", (IExpression)null));
    brokersExpr = Xlate.get( config, "brokers", Xlate.childGet( config, "brokers", (IExpression)null));
    requestsQueueExpr = Xlate.get( config, "requests", Xlate.childGet( config, "requests", (IExpression)null));
    responsesQueueExpr = Xlate.get( config, "responses", Xlate.childGet( config, "responses", (IExpression)null));
    
    connectionFactory = new ConnectionFactory();
    connectionFactory.setHost( "localhost");
  }

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
      final IXAction onUnregister)
  {
    int threads = (threadsExpr != null)? (int)threadsExpr.evaluateNumber( context): 1;
    boolean ssl = (sslExpr != null)? sslExpr.evaluateBoolean( context): false;
   
    String requestsQueue = requestsQueueExpr.evaluateString( context);
    String responsesQueue = responsesQueueExpr.evaluateString( context);
    
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
    
    try
    {
      ExecutorService ioExecutor = (threads == 0)? Executors.newCachedThreadPool(): Executors.newFixedThreadPool( threads);
      Address[] brokers = getBrokers( context);
      Connection connection = (brokers == null)?
          connectionFactory.newConnection( ioExecutor):
          connectionFactory.newConnection( ioExecutor, brokers);
          
      AmqpXioChannel channel = new AmqpXioChannel( connection, "", requestsQueue, responsesQueue);
      AmqpXioPeer peer = new AmqpXioPeer( channel, registry, context, context.getExecutor(), null, null);
      channel.setPeer( peer);
      
      channel.createServiceQueues( name);
      register( context, peer, name, onError);
      
      return peer;
    }
    catch( IOException e)
    {
      log.exception( e);
      // TODO:
    }
    
    return null;
  }

  /**
   * Register the peer.
   * @param context The context.
   * @param peer The peer.
   * @param name The registration name.
   * @param onError The error handler.
   */
  private void register( final IContext context, XioPeer peer, String name, final IXAction onError)
  {
    try
    {
      peer.register( name);
    }
    catch( Exception e)
    {
      context.getExecutor().execute( new Runnable() {
        public void run()
        {
          if ( onError != null) onError.run( context);
        }
      });
    }
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
  
  private static Log log = Log.getLog( AmqpClientTransport.class);
  
  private ConnectionFactory connectionFactory;
  private IExpression requestsQueueExpr;
  private IExpression responsesQueueExpr;
  private IExpression threadsExpr;
  private IExpression sslExpr;
  private IExpression brokersExpr;
  private IXioPeerRegistry registry;
}
