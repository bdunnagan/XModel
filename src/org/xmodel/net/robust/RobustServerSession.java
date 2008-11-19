/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.net.robust;

import java.io.IOException;
import java.net.Socket;

/**
 * An extension of RobustSession which can be used as a ServerSession.
 */
public class RobustServerSession extends RobustSession implements IServerSession
{
  /**
   * Create a blocking, bounded queue on the specified session.
   * The session is owned by this instance and will be closed when the queue is closed.
   * @param session The session.
   * @param queueLength The maximum length of the queue.
   */
  public RobustServerSession( ServerSession session, int queueLength)
  {
    super( session, queueLength);
    server = session.getServer();
    session.addListener( handler);
  }

  /**
   * Create a blocking, bounded queue on the specified session.
   * The session is owned by this instance and will be closed when the queue is closed.
   * @param session The session.
   * @param queueLength The maximum length of the queue.
   * @param writeTimeout The time to wait before write failure (-1 means infinite).
   */
  public RobustServerSession( ServerSession session, int queueLength, int writeTimeout)
  {
    super( session, queueLength, writeTimeout);
    server = session.getServer();
    session.addListener( handler);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.robust.IServerSession#getServer()
   */
  public Server getServer()
  {
    return ((IServerSession)session).getServer();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IServerSession#initialize(java.net.Socket)
   */
  public void initialize( Socket newSocket) throws IOException
  {
    ((ServerSession)getDelegate()).initialize( newSocket);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.robust.IServerSession#setReconnected(boolean)
   */
  public void setReconnected( boolean reconnected)
  {
    ((ServerSession)getDelegate()).setReconnected( true);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.robust.ISession#isReconnected()
   */
  public boolean isReconnected()
  {
    return ((ServerSession)getDelegate()).isReconnected();
  }

  private final ISession.IListener handler = new ISession.IListener() {
    public void notifyOpen( ISession session)
    {
    }
    public void notifyClose( ISession session)
    {
      server.removeSession( RobustServerSession.this);
    }
    public void notifyConnect( ISession session)
    {
    }
    public void notifyDisconnect( ISession session)
    {
    }
  };
  
  private Server server;
}
