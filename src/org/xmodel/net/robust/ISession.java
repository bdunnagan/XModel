/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ISession.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.net.robust;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

public interface ISession
{
  /**
   * Returns the unique session number. Each client generates a unique session number 
   * which will represent the client on the server.
   * @return Returns the unique session number.
   */
  public long getSessionNumber();
  
  /**
   * Returns the session number converted into a short hash which might overlap (for readability).
   * @return Returns the session number converted into a short hash which might overlap.
   */
  public String getShortSessionID();
  
  /**
   * Returns the session number converted into an string that will not overlap.
   * @return Returns the session number converted into an string that will not overlap.
   */
  public String getLongSessionID();
  
  /**
   * Add a handler.
   * @param listener The handler.
   */
  public void addListener( IListener listener);
  
  /**
   * Remove a handler.
   * @param listener The handler.
   */
  public void removeListener( IListener listener);
  
  /**
   * Returns the handler list.
   * @return Returns the handler list.
   */
  public List<IListener> getHandlers();

  /**
   * Returns the remote address.
   * @return Returns the remote address.
   */
  public InetSocketAddress getRemoteAddress();

  /**
   * Open this session. This method does not block.
   */
  public void open() throws IOException;

  /**
   * Close this session. This method may or may not block until the session closes.
   */
  public void close();

  /**
   * Returns true if the session is open.
   * @return Returns true if the session is open.
   */
  public boolean isOpen();
  
  /**
   * Returns true if this session has been successfully reconnected.
   * @return Returns true if this session has been successfully reconnected.
   */
  public boolean isReconnected();
  
  /**
   * Close the underlying socket and force the session to reestablish. This method
   * is useful if socket parameters have been changed and the socket needs to be
   * reinitialized.
   */
  public void bounce();
  
  /**
   * Read bytes from the remote peer. If the remote peer has become disconnected 
   * then this method will return -1.
   * @param buffer The buffer.
   * @return Returns the number of bytes read or -1 if disconnected.
   */
  public int read( byte[] buffer);
  
  /**
   * Read bytes from the remote peer. If the remote peer has become disconnected
   * then this method will return -1.
   * @param buffer The buffer.
   * @param offset The offset.
   * @param length The number of bytes to read.
   * @return Returns the number of bytes read or -1 if disconnected.
   */
  public int read( byte[] buffer, int offset, int length);
  
  /**
   * Send bytes to the remote peer.
   * @param buffer The buffer.
   * @return Returns true if the send was successful.
   */
  public boolean write( byte[] buffer);
  
  /**
   * Send bytes to the remote peer.
   * @param buffer The buffer.
   * @param offset The offset into the buffer.
   * @param length The number of bytes to send.
   * @return Returns true if the send was successful.
   */
  public boolean write( byte[] buffer, int offset, int length);
  
  /**
   * Set the stream factory which creates the streams associated with this session.
   * These are the streams that are returned by the <code>getInputStream</code> and
   * <code>getOutputStream</code> methods. This method allows the client to associate
   * stream wrappers such as DataInputStream and DataOutputStream with the session.
   * @param factory The factory.
   */
  public void setStreamFactory( StreamFactory factory);
  
  /**
   * Returns the input stream for this session.
   * @return Returns the input stream for this session.
   */
  public InputStream getInputStream();
  
  /**
   * Returns the output stream for this session.
   * @return Returns the output stream for this session.
   */
  public OutputStream getOutputStream();
  
  public static interface StreamFactory
  {
    /**
     * Returns an input stream which wraps the specified stream.
     * @param stream The input stream for the session.
     * @return Returns an input stream which wraps the specified stream.
     */
    public InputStream getInputStream( InputStream stream);

    /**
     * Returns an output stream which wraps the specified stream.
     * @param stream The output stream for the session.
     * @return Returns an output stream which wraps the specified stream.
     */
    public OutputStream getOutputStream( OutputStream stream);
  }
  
  /**
   * An interface for handling messages from the remote peer.
   */
  public static interface IListener
  {
    /**
     * Called when the session is opened.
     * @param session The session.
     */
    public void notifyOpen( ISession session);
    
    /**
     * Called when the session is closed.
     * @param session The session.
     */
    public void notifyClose( ISession session);
    
    /**
     * Called when the session is connected to the remote peer.
     * @param session The session.
     */
    public void notifyConnect( ISession session);
    
    /**
     * Called when the session is disconnected from the remote peer.
     * @param session The session.
     */
    public void notifyDisconnect( ISession session);
  }
  
  public final static boolean debug = true;
}
