/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * TestClient.java
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

@SuppressWarnings("unused")
public class TestClient
{
  private static Simulator sim;
  
  public static void test2( String[] args) throws Exception
  {
    ISession session = new Client( "127.0.0.1", 10000);
    session = new RobustSession( session, 1);
    
    session.addListener( new ISession.IListener() {
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
    });
    
    session.open();

    byte[] data = new byte[ 1];
    for( int i=0; i<128; i++)
    {
      data[ 0] = (byte)i;
      System.out.printf( "SEND %d\n", i);
      session.write( data);
    }
    
    Thread.sleep( 100000000);
  }
  
  public static void main( String[] args) throws Exception
  {
    test2( args);
  }
}
