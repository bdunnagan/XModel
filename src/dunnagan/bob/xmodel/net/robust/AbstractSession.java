/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.robust;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class for ISession.
 */
public abstract class AbstractSession implements ISession
{
  protected AbstractSession()
  {
    handlers = new ArrayList<Listener>();
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.nu.ISession#addHandler(dunnagan.bob.xmodel.net.nu.ISession.Handler)
   */
  public void addListener( Listener listener)
  {
    if ( !handlers.contains( listener)) handlers.add( listener);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.nu.ISession#removeHandler(dunnagan.bob.xmodel.net.nu.ISession.Handler)
   */
  public void removeListener( Listener listener)
  {
    handlers.remove( listener);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.nu.ISession#getHandlers()
   */
  public List<Listener> getHandlers()
  {
    return handlers;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.nu.ISession#read(byte[])
   */
  public int read( byte[] buffer)
  {
    InputStream input = getSocketInputStream();
    if ( input == null) return -1;
    
    try
    {
      int count =  input.read( buffer);
      //System.err.printf( "read %d\n", count);
      return count;
    }
    catch( IOException e)
    {
      if ( debug) e.printStackTrace( System.err);
      notifyDisconnect();
      return -1;
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.nu.ISession#read(byte[], int, int)
   */
  public int read( byte[] buffer, int offset, int length)
  {
    InputStream input = getSocketInputStream();
    if ( input == null) return -1;
    
    try
    {
      int count =  input.read( buffer, offset, length);
      //System.err.printf( "read %d\n", count);
      return count;
    }
    catch( IOException e)
    {
      if ( debug) e.printStackTrace( System.err);
      notifyDisconnect();
      return -1;
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.nu.ISession#send(byte[])
   */
  public boolean write( byte[] buffer)
  {
    OutputStream output = getSocketOutputStream();
    if ( output == null) return false;
    
    try
    {
      output.write( buffer);
      //System.err.printf( "wrote %d\n", buffer.length);
      output.flush();
      return true;
    }
    catch( IOException e)
    {
      if ( debug) e.printStackTrace( System.err);
      notifyDisconnect();
      return false;
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.nu.ISession#send(byte[], int, int)
   */
  public boolean write( byte[] buffer, int offset, int length)
  {
    OutputStream output = getSocketOutputStream();
    if ( output == null) return false;
    
    try
    {
      output.write( buffer, offset, length);
      //System.err.printf( "wrote %d\n", length);
      output.flush();
      return true;
    }
    catch( IOException e)
    {
      if ( debug) e.printStackTrace( System.err);
      notifyDisconnect();
      return false;
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.nu.ISession#setStreamFactory(dunnagan.bob.xmodel.net.nu.ISession.StreamFactory)
   */
  public void setStreamFactory( StreamFactory factory)
  {
    this.factory = factory;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.nu.ISession#getInputStream()
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
   * @see dunnagan.bob.xmodel.net.nu.ISession#getOutputStream()
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
   * Returns the input stream or null.
   * @return Returns the input stream or null.
   */
  protected abstract InputStream getSocketInputStream();
  
  /**
   * Returns the output stream or null.
   * @return Returns the output stream or null.
   */
  protected abstract OutputStream getSocketOutputStream();
  
  /**
   * Notify handler that session was opened.
   */
  protected void notifyOpen()
  {
    Listener[] handlers = this.handlers.toArray( new Listener[ 0]);
    for( Listener handler: handlers)
    {
      try
      {
        if ( handler != null) handler.notifyOpen( this);
      }
      catch( Exception e)
      {
        e.printStackTrace( System.err);
      }
    }
  }
  
  /**
   * Notify handler that session was closed.
   */
  protected void notifyClose()
  {
    Listener[] handlers = this.handlers.toArray( new Listener[ 0]);
    for( Listener handler: handlers)
    {
      try
      {
        if ( handler != null) handler.notifyClose( this);
      }
      catch ( Exception e)
      {
        e.printStackTrace( System.err);
      }
    }
  }
  
  /**
   * Notify handler that connection was completed and set open flag.
   */
  protected void notifyConnect()
  {
    Listener[] handlers = this.handlers.toArray( new Listener[ 0]);
    for( Listener handler: handlers)
    {
      try
      {
        if ( handler != null) handler.notifyConnect( this);
      }
      catch( Exception e)
      {
        e.printStackTrace( System.err);
      }
    }
  }
  
  /**
   * Notify handler that connection was broken and clear open flag.
   */
  protected void notifyDisconnect()
  {
    Listener[] handlers = this.handlers.toArray( new Listener[ 0]);
    for( Listener handler: handlers)
    {
      try
      {
        if ( handler != null) handler.notifyDisconnect( this);
      }
      catch( Exception e)
      {
        e.printStackTrace( System.err);
      }
    }
  }
  
  private List<Listener> handlers;
  private StreamFactory factory;
  private InputStream sessionIn;
  private OutputStream sessionOut;
}
