/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.net.robust;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;

/**
 * An implementation of ISession which connects as a client with a TCP socket.
 */
public class Client extends AbstractSession
{
  /**
   * Create a ClientSession for the specified remote peer.
   * @param host The remote host.
   * @param port The remote port.
   */
  public Client( String host, int port)
  {
    // generate session number
    Random random = new Random();
    sid = random.nextLong() & 0x7fffffffffffffffL;
    
    // set default socket flags
    settings = new SocketSettings();
    
    // set address
    address = new InetSocketAddress( host, port);
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
   * @see org.xmodel.net.ISession#open()
   */
  public void open()
  {
    if ( open) return;
    notifyOpen();
    connect();
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
      exit = true;
      if ( thread != null) { thread.interrupt(); thread.join(); }
      if ( socket != null) socket.close();
      
      // notify
      notifyClose();
    }
    catch( Exception e)
    {
    }
    
    socket = null;
    input = null;
    output = null;
    thread = null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.robust.ISession#isOpen()
   */
  public boolean isOpen()
  {
    return open;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.ISession#blink()
   */
  public void bounce()
  {
    try { if ( socket != null) socket.close();} catch( Exception e) {}
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.AbstractSession#getInputStream()
   */
  @Override
  protected synchronized InputStream getSocketInputStream()
  {
    return input;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.AbstractSession#getOutputStream()
   */
  protected synchronized OutputStream getSocketOutputStream()
  {
    return output;
  }

  /**
   * Create thread which will continously attempt to connect.
   */
  private void connect()
  {
    exit = false;
    thread = new Thread( connectRunnable, "Connect");
    thread.setDaemon( true);
    thread.start();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.AbstractSession#notifyDisconnect()
   */
  @Override
  protected void notifyDisconnect()
  {
    super.notifyDisconnect();
    if ( open) connect();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.AbstractSession#notifyClose()
   */
  @Override
  protected void notifyClose()
  {
    super.notifyClose();
    open = false;
  }

  private Runnable connectRunnable = new Runnable() {
    public void run()
    {
      while( !exit)
      {
        try
        {
          Socket newSocket = new Socket();
          settings.configure( newSocket);
          newSocket.connect( address, poll);
          
          synchronized( this)
          {
            socket = newSocket;
            input = socket.getInputStream();
            output = socket.getOutputStream();
          }

          // send session number
          DataOutputStream stream = new DataOutputStream( output);
          stream.writeLong( sid);
          stream.flush();
          
          open = true;
          notifyConnect();
          
          return;
        }
        catch( IOException e)
        {
          e.printStackTrace( System.err);
          exit = true;
          notifyClose();
        }
      }
      
      open = false;
    }
  };
  
  private final static int poll = 3000;

  private long sid;
  private SocketSettings settings;
  private InetSocketAddress address;
  private Socket socket;
  private InputStream input;
  private OutputStream output;
  private Thread thread;
  private boolean open;
  private boolean exit;
}