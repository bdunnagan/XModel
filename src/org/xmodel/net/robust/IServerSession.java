/*
 * Stonewall Networks, Inc.
 *
 *   Project: XModel
 *   Author:  bdunnagan
 *   Date:    Feb 25, 2008
 *
 * Copyright 2008.  Stonewall Networks, Inc.
 */
package org.xmodel.net.robust;

import java.io.IOException;
import java.net.Socket;

/**
 * An extension of ISession for server sessions which adds a single method that
 * allows the Server instance to replace the underlying implementation.
 */
public interface IServerSession extends ISession
{
  /**
   * Returns the server which created this session.
   * @return Returns the server which created this session.
   */
  public Server getServer();
  
  /**
   * Reestablish the session on the specified socket.
   * @param newSocket The new socket.
   */
  public void initialize( Socket newSocket) throws IOException;
  
  /**
   * Set the reconnected flag.
   * @param reconnected True if the session has been reconnected.
   */
  public void setReconnected( boolean reconnected);
}
