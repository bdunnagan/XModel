/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * Server.java
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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

import org.xmodel.log.Log;
import org.xmodel.net.robust.ISession.StreamFactory;


public class Server
{
  public Server()
  {
    sessions = Collections.synchronizedMap( new HashMap<String, IServerSession>());
    listeners = new ArrayList<IListener>();
  }

  /**
   * Add a handler for server notifications.
   * @param handler The handler.
   */
  public void addHandler( IListener handler)
  {
    if ( !listeners.contains( handler)) listeners.add( handler);
  }

  /**
   * Remove a handler.
   * @param handler The handler.
   */
  public void removeHandler( IListener handler)
  {
    listeners.remove( handler);
  }
  
  /**
   * Set the server session factory.
   * @param factory The factory.
   */
  public void setSessionFactory( SessionFactory factory)
  {
    sessionFactory = factory;
  }
  
  /**
   * Set the stream factory to be applied to new sessions.
   * @param factory The stream factory.
   */
  public void setStreamFactory( StreamFactory factory)
  {
    streamFactory = factory;
  }
  
  /**
   * Start the server on the specified port.
   * @param port The port.
   */
  public void start( int port) throws IOException
  {
    exit = false;
    timer = new Timer( true);
    serverSocket = new ServerSocket( port);
    serverThread = new Thread( serverRunnable, "XModel Server ("+port+")");
    serverThread.start();
  }
  
  /**
   * Stop the server.
   */
  public void stop()
  {
    try
    {
      exit = true;
      timer.cancel();
      serverSocket.close();
      serverThread.join();
      sessions.clear();
    }
    catch( InterruptedException e)
    {
    }
    catch( IOException e)
    {
      log.exception( e);
    }
    
    timer = null;
    serverSocket = null;
    serverThread = null;
  }
  
  /**
   * Returns the session associated with the specified host. 
   * @param host The remote host.
   * @return Returns the session associated with the specified host.
   */
  public ISession getSession( String host)
  {
    return sessions.get( host);
  }
  
  /**
   * Returns all the active sessions.
   * @return Returns all the active sessions.
   */
  public List<IServerSession> getSessions()
  {
    List<IServerSession> list = new ArrayList<IServerSession>();
    list.addAll( sessions.values());
    return list;
  }
  
  /**
   * Returns the server timer (used by sessions to timeout).
   * @return Returns the server timer (used by sessions to timeout).
   */
  public Timer getTimer()
  {
    return timer;
  }
  
  /**
   * Remove the specified session from the server.
   * @param session The session.
   */
  protected void removeSession( IServerSession session)
  {
    InetSocketAddress address = session.getRemoteAddress();
    long sid = session.getSessionNumber();
    String id = address.getHostName()+":"+sid;
    sessions.remove( id);
    if ( sessions.size() == 0) notifyIdle();
  }      
  
  /**
   * Returns the ServerSession associated with the specified host and session number.
   * @param address The remote address.
   * @param sid The session number.
   * @return Returns the ServerSession.
   */
  private IServerSession getCreateSession( InetSocketAddress address, long sid) throws IOException
  {
    String id = address.getHostName()+":"+sid;
    IServerSession session = sessions.get( id);
    if ( session == null)
    {
      session = (sessionFactory == null)? 
        new ServerSession( this, address, sid): 
        sessionFactory.createSession( this, address, sid);
        
      session.setStreamFactory( streamFactory);  
        
      sessions.put( id, session);
      session.open();
    }
    else
    {
      session.setReconnected( true);
    }
    return session;
  }
  
  /**
   * Read the session number from the session.
   * @param input The socket input stream.
   * @return Returns -1 or session number.
   */
  private long readSessionNumber( InputStream input)
  {
    try
    {
      DataInputStream stream = new DataInputStream( input);
      return stream.readLong();
    }
    catch( IOException e)
    {
      log.exception( e);
    }
    return -1;
  }
  
  /**
   * Notify handlers that a new session was accepted.
   * @param session The session that was accepted.
   */
  private void notifySession( IServerSession session)
  {
    IListener[] handlers = this.listeners.toArray( new IListener[ 0]);
    for( IListener handler: handlers)
    {
      try
      {
        handler.notifyAccept( session);
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }
  
  /**
   * Notify handlers that the server is idle.
   */
  private void notifyIdle()
  {
    IListener[] handlers = this.listeners.toArray( new IListener[ 0]);
    for( IListener handler: handlers)
    {
      try
      {
        handler.notifyIdle();
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
  }
  
  private Runnable serverRunnable = new Runnable() {
    public void run()
    {
      while( !exit)
      {
        try
        {
          Socket socket = serverSocket.accept();
          InetAddress address = socket.getInetAddress();
          int port = serverSocket.getLocalPort();
          
          // read session identifier
          long sid = readSessionNumber( socket.getInputStream());
          
          // find or create session
          IServerSession session = getCreateSession( new InetSocketAddress( address, port), sid);
          notifySession( session);
          
          // send reconnected flag
          socket.getOutputStream().write( session.isReconnected()? 1: 0);
          
          // establish session (notify connection)
          session.initialize( socket);
        }
        catch( SocketException e)
        {
          if ( !exit) log.exception( e);
          return;
        }
        catch( IOException e)
        {
          log.exception( e);
          return;
        }
      }
    }
  };
  
  public static interface SessionFactory
  {
    /**
     * Create an instance of IServerSession.
     * @param server The server.
     * @param address The remote address.
     * @param sid The session number.
     * @return Returns the new IServerSession.
     */
    public IServerSession createSession( Server server, InetSocketAddress address, long sid);
  }
  
  public static interface IListener
  {
    /**
     * Called when a new session is accepted.
     * @param session The session.
     */
    public void notifyAccept( IServerSession session);
    
    /**
     * Called after the last session closes.
     */
    public void notifyIdle();
  }
  
  private static Log log = Log.getLog( "org.xmodel.net.robust");
  
  private Thread serverThread;
  private ServerSocket serverSocket;
  private List<IListener> listeners;
  private SessionFactory sessionFactory;
  private StreamFactory streamFactory;
  private Map<String, IServerSession> sessions;
  private Timer timer;
  private boolean exit;
}

