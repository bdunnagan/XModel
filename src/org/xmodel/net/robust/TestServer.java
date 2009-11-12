/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * TestServer.java
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

import java.net.InetSocketAddress;
import org.xmodel.net.robust.Server.SessionFactory;

@SuppressWarnings("unused")
public class TestServer
{
  private static Simulator sim;
  
  public static void test2( String[] args) throws Exception
  {
    Server server = new Server();

    final ISession.IListener handler = new ISession.IListener() {
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
