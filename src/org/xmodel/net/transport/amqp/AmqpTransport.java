package org.xmodel.net.transport.amqp;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.net.IXioPeerRegistry;
import org.xmodel.net.MemoryXioPeerRegistry;
import org.xmodel.net.OpenTrustStore;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.ConnectionFactory;

abstract class AmqpTransport
{
  protected AmqpTransport()
  {
    registry = new AmqpXioPeerRegistry( new MemoryXioPeerRegistry());
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
    timeoutExpr = Xlate.get( config, "timeout", Xlate.childGet( config, "timeout", (IExpression)null));
    
    connectionFactory = new ConnectionFactory();
    connectionFactory.setHost( "localhost");
  }
  
  /**
   * @return Returns null or the array of brokers that will be tried.
   * @param context The context.
   */
  protected Address[] getBrokers( IContext context)
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
  protected SSLContext getSSLContext( IContext context)
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
  
  protected ConnectionFactory connectionFactory;
  protected IExpression queueExpr;
  protected IExpression threadsExpr;
  protected IExpression sslExpr;
  protected IExpression timeoutExpr;
  protected IExpression brokersExpr;
  protected IXioPeerRegistry registry;
}
