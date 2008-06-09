/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.robust;

import java.net.InetSocketAddress;

import dunnagan.bob.xmodel.net.robust.Server.SessionFactory;

public class TestServer
{
  private static Simulator sim;
  
  public static void test2( String[] args) throws Exception
  {
    Server server = new Server();

    final ISession.Listener handler = new ISession.Listener() {
      public void notifyOpen( ISession session)
      {
        System.out.printf( "OPENED: %X\n", session.getSessionNumber());
      }
      public void notifyClose( ISession session)
      {
        System.out.printf( "CLOSED: %X\n", session.getSessionNumber());
      }
      public void notifyConnect( ISession session)
      {
        System.out.printf( "CONNECTED: %X\n", session.getSessionNumber());
      }
      public void notifyDisconnect( ISession session)
      {
        System.out.printf( "DISCONNECTED: %X\n", session.getSessionNumber());
      }
    };

    server.setSessionFactory( new SessionFactory() {
      public IServerSession createSession( Server server, InetSocketAddress address, long sid)
      {
        return new RobustServerSession( new ServerSession( server, address, sid), 1);
      }
    });
    
    server.addHandler( new ServerHandler( handler) {
      public void run( IServerSession session)
      {
        byte[] buffer = new byte[ 100];
        int count = 0;
        while( count >= 0)
        {
          count = session.read( buffer);
          for( int i=0; i<count; i++)
            System.out.printf( "RECV: %x %d\n", session.getSessionNumber(), buffer[ i]);
        }
      }
    });
    
    server.start( 10000);
  }
  
  public static void main( String[] args) throws Exception
  {
    test2( args);
  }
}
