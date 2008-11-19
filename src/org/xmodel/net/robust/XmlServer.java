/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
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
