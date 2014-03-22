package org.xmodel.net.connection;

import org.xmodel.future.AsyncFuture;

public interface INetworkConnection
{
  public interface IListener
  {
    /**
     * Called when a message is received.
     * @param connection The connection that received the message.
     * @param message The message that was received.
     */
    public void onMessageReceived( INetworkConnection connection, INetworkMessage message);
    
    /**
     * Called when the network connection is closed either explicitly or unexpectedly.
     * @param connection The connection that was closed.
     * @param cause A connection close message (such as <i>closedFutureMessage</i>, or an exception.
     */
    public void onClose( INetworkConnection connection, Object cause);
  }
  
  /**
   * Reason for request future failure when connection is closed before response is received.
   */
  public final static String closedFutureMessage = "closed";
  
  /**
   * Establish the network connection. If the connection is already established, then this
   * method should return a SuccessAsyncFuture indicating that the connection is already 
   * established.
   * @return Returns the future for the operation.
   */
  public AsyncFuture<INetworkConnection> connect();
  
  /**
   * Close this connection.
   * @return Returns the future for the operation.
   */
  public AsyncFuture<INetworkConnection> close();
  
  /**
   * Send a message.
   * @param message The message.
   */
  public void send( INetworkMessage message) throws Exception;
  
  /**
   * Send a request and return a future that will be notified when the response arrives.
   * @param request The request message.
   * @param timeout The timeout for the request in milliseconds (excessive values will cause memory leak).
   * @return Returns a future for the response message.
   */
  public AsyncFuture<INetworkMessage> request( INetworkMessage request, int timeout);
  
  /**
   * Add a listener for incoming messages.
   * @param listener The listener.
   */
  public void addListener( IListener listener);
  
  /**
   * Remove a listener for incoming messages.
   * @param listener The listener.
   */
  public void removeListener( IListener listener);
  
  /**
   * @return Returns an array containing the listeners.
   */
  public IListener[] getListeners();
}
