/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.net.robust;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.TimerTask;

/**
 * An implementation of ISession for the server-side.
 */
public class ServerSession extends AbstractSession implements IServerSession
{
  public ServerSession( Server server, InetSocketAddress address, long sid)
  {
    this.server = server;
    this.address = address;
    this.sid = sid;
    this.timeout = 10000;

    // set default socket flags
    settings = new SocketSettings();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.ISession#getRemoteAddress()
   */
  public InetSocketAddress getRemoteAddress()
  {
    return address;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.ISession#getSessionNumber()
   */
  public long getSessionNumber()
  {
    return sid;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.robust.IServerSession#getServer()
   */
  public Server getServer()
  {
    return server;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.ISession#open()
   */
  public void open()
  {
    if ( open) return;
    open = true;
    notifyOpen();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.ISession#close()
   */
  public void close()
  {
    if ( !open) return;
    open = false;
    
    try
    {
      timeout = -1; // disable disconnect timer
      socket.close();
    }
    catch( Exception e)
    {
      if ( debug) e.printStackTrace( System.err);
    }
    
    // cleanup
    socket = null;
    input = null;
    output = null;
    
    // tell server
    server.removeSession( this);
    
    // notify
    notifyClose();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.robust.ISession#isOpen()
   */
  public boolean isOpen()
  {
    return open;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.robust.ISession#isReconnected()
   */
  public boolean isReconnected()
  {
    return reconnected;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.robust.IServerSession#setReconnected(boolean)
   */
  public void setReconnected( boolean reconnected)
  {
    this.reconnected = reconnected;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.ISession#blink()
   */
  public void bounce()
  {
    try { if ( socket != null) socket.close();} catch( Exception e) {}
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.IServerSession#initialize(java.net.Socket)
   */
  public void initialize( Socket newSocket) throws IOException
  {
    if ( socket != null)
    {
      // this means we did not know that we were not connected
      try
      {
        socket.close();
      }
      catch( Exception e)
      {
        if ( debug) e.printStackTrace( System.err);
      }
    }
    
    // set connection objects
    synchronized( this)
    {
      this.input = newSocket.getInputStream();
      this.output = newSocket.getOutputStream();
      socket = newSocket;
      settings.configure( socket);
    }

    // notify
    notifyConnect();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.AbstractSession#getInputStream()
   */
  @Override
  protected InputStream getSocketInputStream()
  {
    return input;
  }

  /**
   * Returns the output stream safely.
   * @return Returns the output stream safely.
   */
  protected synchronized OutputStream getSocketOutputStream()
  {
    return output;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.AbstractSession#notifyConnect()
   */
  @Override
  protected void notifyConnect()
  {
    // cancel timeout timer
    if ( task != null) task.cancel();
    
    // notify
    super.notifyConnect();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.AbstractSession#notifyDisconnect()
   */
  @Override
  protected void notifyDisconnect()
  {
    // notify
    super.notifyDisconnect();

    // set session timeout timer
    if ( timeout >= 0)
    {
      task = new TimeoutTask();
      server.getTimer().schedule( task, timeout);
    }
  }

  private class TimeoutTask extends TimerTask
  {
    public void run()
    {
      close();
    }
  }
  
  private SocketSettings settings;  
  private Server server;
  private InetSocketAddress address;
  private long sid;
  private Socket socket;
  private InputStream input;
  private OutputStream output;
  private boolean open;
  private boolean reconnected;
  private TimeoutTask task;
  private int timeout;
}
