/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.robust;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of ISession which delegates to another implementation of ISession.
 * The <code>write</code> methods queue data until it can be sent over an operating
 * connection.  The <code>read</code> methods will block until all requested data has
 * been read even if the connection is temporarily dropped. 
 */
public class RobustSession implements ISession
{
  /**
   * Create a session which will function when the connection is down. Read operations
   * will only return -1 when the session has been closed. Write operations write to an
   * internal queue. When the queue is full, a write operation on the session will block.
   * The session is owned by this instance and will be closed when the queue is closed.
   * @param session The session.
   * @param queueLength The maximum length of the queue.
   */
  public RobustSession( ISession session, int queueLength)
  {
    this( session, queueLength, -1);
  }

  /**
   * Create a session which will function when the connection is down. Read operations
   * will only return -1 when the session has been closed. Write operations write to an
   * internal queue. When the queue is full, the write timeout specifies the amount of
   * time to wait before declaring the write operation failed.
   * The session is owned by this instance and will be closed when the queue is closed.
   * @param session The session.
   * @param queueLength The maximum length of the queue.
   * @param writeTimeout The time to wait before write failure (-1 means infinite).
   */
  public RobustSession( ISession session, int queueLength, int writeTimeout)
  {
    this.session = session;
    this.queue = new ArrayBlockingQueue<byte[]>( queueLength);
    this.blocker = new Semaphore( 0);
    this.writeTimeout = writeTimeout;
    
    session.addListener( handler);
  }

  /**
   * Returns the instance of ISession passed to the constructor.
   * @return Returns the instance of ISession passed to the constructor.
   */
  public ISession getDelegate()
  {
    return session;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.ISession#getRemoteAddress()
   */
  public InetSocketAddress getRemoteAddress()
  {
    return session.getRemoteAddress();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.ISession#getSessionNumber()
   */
  public long getSessionNumber()
  {
    return session.getSessionNumber();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.robust.ISession#getLongSessionID()
   */
  public String getLongSessionID()
  {
    return session.getLongSessionID();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.robust.ISession#getShortSessionID()
   */
  public String getShortSessionID()
  {
    return session.getShortSessionID();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.robust.ISession#isOpen()
   */
  public boolean isOpen()
  {
    return session.isOpen();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.ISession#addHandler(dunnagan.bob.xmodel.net.ISession.Handler)
   */
  public void addListener( Listener listener)
  {
    session.addListener( listener);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.ISession#removeHandler(dunnagan.bob.xmodel.net.ISession.Handler)
   */
  public void removeListener( Listener listener)
  {
    session.removeListener( listener);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.ISession#getHandlers()
   */
  public List<Listener> getHandlers()
  {
    return session.getHandlers();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.ISession#open()
   */
  public void open()
  {
    exit = false;
    session.open();
  }

  /**
   * Close the session queue.
   */
  public void close()
  {
    try
    {
      exit = true;
      session.close();
      if ( thread != null)
      {
        thread.interrupt();
        thread.join();
      }
    }
    catch( Exception e)
    {
    }
    
    thread = null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.ISession#bounce()
   */
  public void bounce()
  {
    session.bounce();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.ISession#read(byte[])
   */
  public int read( byte[] buffer)
  {
    return read( buffer, 0, buffer.length);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.ISession#read(byte[], int, int)
   */
  public int read( byte[] buffer, int offset, int length)
  {
    if ( !connected) waitForConnection();

    while( !exit)
    {
      int count = session.read( buffer, offset, length);
      if ( count > 0) 
      {
        return count;
      }
      else if ( count == 0)
      {
        // precaution against possible busy wait
        try { Thread.sleep( 100);} catch( Exception e2) {}
      }
      else if ( count == -1)
      {
        try { Thread.sleep( 3000);} catch( Exception e2) {}
        if ( !exit && !waitForConnection()) return -1;
      }
    }
    
    return -1;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.AbstractSession#send(byte[])
   */
  public boolean write( byte[] buffer)
  {
    return write( buffer, 0, buffer.length);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.AbstractSession#send(byte[], int, int)
   */
  public boolean write( byte[] buffer, int offset, int length)
  {
    byte[] data = new byte[ length];
    System.arraycopy( buffer, offset, data, 0, length);
    try 
    { 
      if ( writeTimeout < 0)
      {
        queue.put( data);
        return true;
      }
      else
      {
        return queue.offer( data, writeTimeout, TimeUnit.MILLISECONDS);
      }
    }
    catch ( InterruptedException e)
    {
      return false;
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.ISession#setStreamFactory(dunnagan.bob.xmodel.net.ISession.StreamFactory)
   */
  public void setStreamFactory( StreamFactory factory)
  {
    this.factory = factory;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.ISession#getInputStream()
   */
  public InputStream getInputStream()
  {
    if ( sessionIn == null) 
    {
      sessionIn = new SessionInputStream( this);
      if ( factory != null) sessionIn = factory.getInputStream( sessionIn);
    }
    return sessionIn;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.ISession#getOutputStream()
   */
  public OutputStream getOutputStream()
  {
    if ( sessionOut == null) 
    {
      sessionOut = new SessionOutputStream( this);
      if ( factory != null) sessionOut = factory.getOutputStream( sessionOut);
    }
    return sessionOut;
  }

  /**
   * Block until the connection comes back up.
   * @return Returns false if the session was closed.
   */
  private boolean waitForConnection()
  {
    try
    {
      // wait for connection to go down
      while( connected) try { Thread.sleep( 500);} catch( Exception e) {}
      
      // wait for connection to come back up
      blocker.acquire();
      return connected;
    }
    catch( InterruptedException e)
    {
      return false;
    }
  }
  
  private final Runnable dequeueRunnable = new Runnable() {
    public void run()
    {
      while( !exit)
      {
        try
        {
          byte[] data = queue.take();
          if ( !exit && data != null)
            if( !session.write( data)) close();
        }
        catch( InterruptedException e)
        {
        }
      }
    }
  };
  
  private final ISession.Listener handler = new ISession.Listener() {
    public void notifyOpen( ISession session)
    {
      exit = false;
      blocker.drainPermits();
      connected = false;
      queue.clear();
    }
    public void notifyClose( ISession session)
    {
      exit = true;
      connected = false;
      blocker.release();
      
      // stop
      if ( thread != null)
      {
        thread.interrupt();
        try { thread.join();} catch( Exception e) {}
        thread = null;
      }
    }
    public void notifyConnect( ISession session)
    {
      exit = false;
      connected = true;
      blocker.release();
      
      // start
      thread = new Thread( dequeueRunnable, "Queue");
      thread.start();
    }
    public void notifyDisconnect( ISession session)
    {
      exit = true;
      blocker.drainPermits();
      connected = false; 
      
      // stop
      if ( thread != null)
      {
        thread.interrupt();
        try { thread.join();} catch( Exception e) {}
        thread = null;
      }
    }
  };
  
  protected ISession session;
  private Thread thread;
  private ArrayBlockingQueue<byte[]> queue;
  private Semaphore blocker;
  private boolean connected;
  private boolean exit;
  private int writeTimeout;
  private StreamFactory factory;
  private InputStream sessionIn;
  private OutputStream sessionOut;
}
