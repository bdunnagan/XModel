package org.xmodel.net.nu.packet;

import java.util.ArrayList;
import java.util.List;

public abstract class SAR<T>
{
  public SAR()
  {
    packets = new ArrayList<T>();
    offset = -1;
  }
  
  /**
   * Receive a packet.
   * @param sequence The sequence number.
   * @param packet The packet.
   */
  public void receive( int sequence, T packet)
  {
    // initialize offset (sequence numbers begin with 1)
    if ( offset < 0) offset = sequence;
    
    // calculate packet position
    int position = sequence - offset;
    if ( position < 0)
    {
      position = Integer.MAX_VALUE - offset + sequence;
    }
    
    // allocate slots in list
    for( int i=packets.size(); i<=position; i++)
      packets.add( null);
    
    // store packet
    packets.set( position, packet);
    
    // request resend of missing packets
    for( int i=0; i<position; i++)
    {
      if ( packets.get( i) == null)
        resend( offset + i);
    }
    
    // process messages that are in order
    for( int i=0; i<packets.size(); i++)
    {
      T next = packets.get( i);
      if ( next == null) break;
      
      // handle packet
      packets.remove( i--);
      handle( next);
      
      // move window
      if ( offset == Integer.MAX_VALUE) offset = 1; else ++offset;
    }
  }
  
  /**
   * Request that the packet with the specified sequence number be resent.
   * @param sequence The sequence number.
   */
  protected abstract void resend( int sequence);
  
  /**
   * Handle the next packet.
   * @param packet The packet.
   */
  protected abstract void handle( T packet);

  private List<T> packets;
  private int offset;
  
  public static void main( String[] args) throws Exception
  {
    SAR<String> sar = new SAR<String>() {
      protected void handle( String packet)
      {
        System.out.printf( "handle: %s\n", packet);
      }
      protected void resend( int sequence)
      {
        System.out.printf( "resend: %d\n", sequence);
      }
    };
   
    int x = Integer.MAX_VALUE - 1;
    sar.receive( x, "x");
    sar.receive( 1, "1");
    sar.receive( x+1, "x+1");
  }
}
