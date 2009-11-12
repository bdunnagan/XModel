/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * XmlServer.java
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

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.xmodel.IModelObject;

/**
 * A Server which uses a compressed XML message schema.
 */
public class XmlServer extends Server
{
  public XmlServer()
  {
    receivers = new HashMap<String, IReceiver>();

    // use robust sessions
    setSessionFactory( new SessionFactory() {
      public IServerSession createSession( Server server, InetSocketAddress address, long sid)
      {
        return new RobustServerSession( new XmlServerSession( server, address, sid), 100, 100);
      }
    });
  }
  
  /**
   * Register a receiver for a specific message type.
   * @param type The element name of the message root element.
   * @param receiver The receiver that will handle messages received by the host.
   */
  public void register( String type, IReceiver receiver)
  {
    if ( getSessions().size() > 0) throw new IllegalStateException( "Server is running.");
    receivers.put( type, receiver);
  }
  
  /**
   * Unregister the receiver on the specified type.
   * @param type The type.
   */
  public void unregister( String type)
  {
    if ( getSessions().size() > 0) throw new IllegalStateException( "Server is running.");
    receivers.remove( type);
  }

  /**
   * Returns the receiver registered for the specified message type.
   * @param type The element name of the message root element.
   * @return Returns the receiver registered for the specified message type.
   */
  public IReceiver getReceiver( String type)
  {
    return receivers.get( type);
  }
  
  /**
   * An interface for server session message handlers.
   */
  public interface IReceiver
  {
    /**
     * Called when a message is received by a server session.
     * @param server The server.
     * @param session The server session that received the message.
     * @param message The message that was received.
     */
    public void handle( XmlServer server, IServerSession session, IModelObject message);
  }

  private Map<String, IReceiver> receivers;
}
