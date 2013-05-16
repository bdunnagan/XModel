package org.xmodel.xaction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xmodel.future.AsyncFuture;
import org.xmodel.future.AsyncFuture.IListener;
import org.xmodel.net.XioClient;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * Create a client connection and register the client with the server with a peer name.
 * Client connections allow the server to use services of the client on demand.
 */
public class ClientAction extends GuardedAction
{
  public final static int defaultRetries = Integer.MAX_VALUE;
  public final static int[] defaultDelays = { 250, 500, 1000, 2000, 3000, 4000, 5000, 5000, 5000, 5000, 10000};
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    serverHostExpr = document.getExpression( "serverHost", true);
    serverPortExpr = document.getExpression( "serverPort", true);
    clientNameExpr = document.getExpression();
    if ( clientNameExpr == null) clientNameExpr = document.getExpression( "name", true);
    
    onConnect = document.createScript( "serverHost", "serverPort", "name");
    onError = document.createScript( "serverHost", "serverPort", "name");
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( final IContext context)
  {
    final String serverHost = serverHostExpr.evaluateString( context);
    final int serverPort = (int)serverPortExpr.evaluateNumber( context);
    final String clientName = clientNameExpr.evaluateString( context);
    
    XioClient client = new XioClient( context);
    
    AsyncFuture<XioClient> future = client.connect( serverHost, serverPort, defaultRetries, defaultDelays);
    future.addListener( new IListener<XioClient>() {
      public void notifyComplete( AsyncFuture<XioClient> future) throws Exception
      {
        if ( future.isSuccess())
        {
          future.getInitiator().register( clientName);
          addClient( serverHost, serverPort, clientName, future);

          // execute onConnect script
          context.getExecutor().execute( new Runnable() {
            public void run()
            {
              if ( onConnect != null) onConnect.run( context);
            }
          });
        }
        else
        {
          // execute onError script
          context.getExecutor().execute( new Runnable() {
            public void run()
            {
              if ( onError != null) onError.run( context);
            }
          });
        }
      }
    });
    
    return null;
  }
  
  /**
   * Create the map key for the specified client.
   * @param serverHost The server to which the client connected.
   * @param serverPort The server port.
   * @param clientName The name of the client.
   * @return Returns the map key.
   */
  private static String createKey( String serverHost, int serverPort, String clientName)
  {
    return String.format( "%s:%d/%s", serverHost, serverPort, clientName);
  }
  
  /**
   * Add a client.
   * @param serverHost The server to which the client connected.
   * @param serverPort The server port.
   * @param clientName The name of the client.
   * @param future The connection future for the client.
   */
  static void addClient( String serverHost, int serverPort, String clientName, AsyncFuture<XioClient> future)
  {
    clients.put( createKey( serverHost, serverPort, clientName), future);
  }
  
  /**
   * Get a client.
   * @param serverHost The server to which the client connected.
   * @param serverPort The server port.
   * @param clientName The name of the client.
   * @return Returns null or the connection future for the client.
   */
  static AsyncFuture<XioClient> getClient( String serverHost, int serverPort, String clientName)
  {
    return clients.get( createKey( serverHost, serverPort, clientName));
  }
  
  /**
   * Remove a client.
   * @param serverHost The server to which the client connected.
   * @param serverPort The server port.
   * @param clientName The name of the client.
   * @return Returns null or the connection future for the client.
   */
  static AsyncFuture<XioClient> removeClient( String serverHost, int serverPort, String clientName)
  {
    return clients.remove( createKey( serverHost, serverPort, clientName));
  }
  
  static Map<String, AsyncFuture<XioClient>> clients = new ConcurrentHashMap<String, AsyncFuture<XioClient>>(); 
  
  private IExpression serverHostExpr;
  private IExpression serverPortExpr;
  private IExpression clientNameExpr;
  private IXAction onConnect;
  private IXAction onError;
}
