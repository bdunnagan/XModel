/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.net.robust;


/**
 * A convenient implementation of Server.Handler which spawns a new thread for each session
 * and calls the <code>run</code> method which can perform an infinite loop handling session
 * requests.  
 */
public abstract class ServerHandler implements Server.Listener
{
  /**
   * Create a ServerHandler which will install the specified session handler on all sessions.
   * @param sessionListener The session handler.
   */
  public ServerHandler( ISession.Listener sessionListener)
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
  private ISession.Listener sessionListener;
}
