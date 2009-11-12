/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ServerHandler.java
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


/**
 * A convenient implementation of Server.Handler which spawns a new thread for each session
 * and calls the <code>run</code> method which can perform an infinite loop handling session
 * requests.  
 */
public abstract class ServerHandler implements Server.IListener
{
  public ServerHandler()
  {
    this( null);
  }
  
  /**
   * Create a ServerHandler which will install the specified session handler on all sessions.
   * @param sessionListener The session handler.
   */
  public ServerHandler( ISession.IListener sessionListener)
  {
    this.sessionListener = sessionListener;
  }
  
  /**
   * Called from the newly created session thread.
   * @param session The new session.
   */
  protected abstract void run( IServerSession session);
  
  /* (non-Javadoc)
   * @see org.xmodel.net.Server.Handler#notifySession(org.xmodel.net.ISession)
   */
  public void notifyAccept( IServerSession session)
  {
    // set session handler
    if ( sessionListener != null) session.addListener( sessionListener);
    
    // create runnable and spawn thread
    SessionRunnable runnable = new SessionRunnable();
    runnable.session = session;
    thread = new Thread( runnable, "XModel Server Session ("+session.getShortSessionID()+")");
    thread.start();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.robust.Server.Handler#notifyIdle()
   */
  public void notifyIdle()
  {
  }

  private class SessionRunnable implements Runnable
  {
    public void run()
    {
      ServerHandler.this.run( session);
    }
    IServerSession session;
  }
  
  private Thread thread;
  private ISession.IListener sessionListener;
}
