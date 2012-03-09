package org.xmodel.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.xmodel.IDispatcher;
import org.xmodel.IModelObject;
import org.xmodel.ModelRegistry;
import org.xmodel.net.stream.TcpClient;

/**
 * A class that implements the network caching policy protocol. 
 * This class is not thread-safe.
 */
public class Client extends Protocol implements Heartbeat.IListener
{
  /**
   * Create a CachingClient to connect to the specified server.
   * @param host The server host.
   * @param port The server port.
   * @param timeout The timeout for network operations in milliseconds.
   * @param daemon True if client thread should be a daemon thread.
   */
  public Client( String host, int port, int timeout, boolean daemon) throws IOException
  {
    super( timeout);
    
    if ( client == null) 
    {
      client = new TcpClient();
      client.start( daemon);
    }
    
    this.host = host;
    this.port = port;
    
    this.feedback = new ArrayList<IModelObject>( 1);
  }

  /**
   * @return Returns the remote host.
   */
  public String getHost()
  {
    return host;
  }
  
  /**
   * @return Returns the remote port.
   */
  public int getPort()
  {
    return port;
  }
  
  /**
   * Add a node that will receive feedback about the health of the connection. The value of the node
   * is updated with the server delay in milliseconds.  If the connection is lost, then the value
   * is set to -1.
   * @param node The node.
   */
  public synchronized void addFeedbackNode( IModelObject node)
  {
    feedback.add( node);
  }
  
  /**
   * Connect (or reconnect) to the remote host.
   * @return Returns a connected session or null on timeout.
   */
  public Session connect( int timeout) throws IOException
  {
    if ( link == null || !link.isOpen())
    {
      link = client.connect( host, port, timeout, this);
    }
    
    if ( link != null)
    {
      Session session = openSession( link);
      if ( heartbeat == null)
      {
        // 
        // Heartbeat must send on the same thread as the model that owns the session.
        //
        IDispatcher dispatcher = ModelRegistry.getInstance().getModel().getDispatcher();
        heartbeat = new Heartbeat( session, this, dispatcher);
        heartbeat.start();
      }
      return session;
    }
    
    return null;
  }

  /**
   * Disconnect form the remote host.
   */
  public void disconnect() throws IOException
  {
    if ( isConnected()) 
    {
      heartbeat.stop();
      heartbeat = null;
      
      link.close();
      link = null;
    }
  }
  
  /**
   * @return Returns true if the client is connected.
   */
  public boolean isConnected()
  {
    return link != null && link.isOpen();
  }
  
  /**
   * Overridden to let Heartbeat know when there is activity on the connection.
   */
  @Override
  public void onReceive( ILink link, ByteBuffer buffer)
  {
    if ( heartbeat != null) heartbeat.onActivity();
    super.onReceive( link, buffer);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Heartbeat.IListener#onActivity(float)
   */
  @Override
  public void onActivity( float delay)
  {
    updateFeedback( String.format( "%3.1fms", delay));
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Heartbeat.IListener#onLinkDown()
   */
  @Override
  public void onLinkDown()
  {
    updateFeedback( "Down");
    try { disconnect();} catch( Exception e) {}
  }
  
  private void updateFeedback( String status)
  {
    for( IModelObject node: feedback)
    {
      node.getModel().dispatch( new FeedbackRunnable( node, status));
    }
  }
  
  private final static class FeedbackRunnable implements Runnable
  {
    public FeedbackRunnable( IModelObject node, String value)
    {
      this.node = node;
      this.value = value;
    }
    
    @Override
    public void run()
    {
      node.setValue( value);
    }
    
    private IModelObject node;
    private String value;
  }
  
  private static TcpClient client;
  
  private String host;
  private int port;
  private ILink link;
  private Heartbeat heartbeat;
  private List<IModelObject> feedback;
}
