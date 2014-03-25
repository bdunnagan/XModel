package org.xmodel.net.connection;

import java.io.IOException;

import org.xmodel.future.AsyncFuture;

/**
 * An interface for network connections that are used for messaging.  Implementations of this interface
 * are responsible for framing and correlation.  The interface was designed to support message-bus
 * implementations, where framing and correlation may already be provided, as well as socket-based
 * implementations.
 */
public interface INetworkConnection
{
  public interface IListener
  {
    /**
     * Called when a message is received.
     * @param connection The connection that received the message.
     * @param message The message that was received.
     * @param correlation TODO
     */
    public void onMessageReceived( INetworkConnection connection, Object message, Object correlation);
    
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
   * @return Returns the protocol implementation for this connection.
   */
  public INetworkProtocol getProtocol();
  
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
   * Returns the future for the next close operation. This instance will change if the connection
   * is closed and subsequently connected again.
   * @return Returns the future for the close operation.
   */
  public AsyncFuture<INetworkConnection> getCloseFuture();
  
  /**
   * Send a message.
   * @param message The message.
   */
  public void send( Object message) throws IOException;
  
  /**
   * Send a request and return a future that will be notified when the response arrives.
   * @param request The request message.
   * @param correlation The correlation key.
   * @return Returns a future for the operation (with a reference to the request message).
   */
  public RequestFuture request( Object request, Object correlation);
    
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
