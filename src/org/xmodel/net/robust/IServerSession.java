/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IServerSession.java
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
