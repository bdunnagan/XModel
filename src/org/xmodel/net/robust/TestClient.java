/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
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
