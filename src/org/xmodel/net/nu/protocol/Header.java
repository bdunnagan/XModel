package org.xmodel.net.nu.protocol;


final class Header
{
  public static int readInt( byte[] bytes, int offset)
  {
    int i;
    i = (int)bytes[ offset++] & 0xFF;
    i |= ((int)bytes[ offset++] & 0xFF) << 8;
    i |= ((int)bytes[ offset++] & 0xFF) << 16;
    i |= ((int)bytes[ offset++] & 0xFF) << 24;
    return i;
  }
  
  public static void writeInt( int i, byte[] bytes, int offset)
  {
    bytes[ offset++] = (byte)(i & 0xFF); i >>= 8;
    bytes[ offset++] = (byte)(i & 0xFF); i >>= 8;
    bytes[ offset++] = (byte)(i & 0xFF); i >>= 8;
    bytes[ offset++] = (byte)(i & 0xFF);
  }
  
  public final static void main( String[] args) throws Exception
  {
    byte[] bytes = new byte[ 4];
    
    Header.writeInt( 1, bytes, 0);
    System.out.println( Header.readInt( bytes, 0) == 1);
    
    Header.writeInt( 256, bytes, 0);
    System.out.println( Header.readInt( bytes, 0) == 256);
    
    Header.writeInt( -1, bytes, 0);
    System.out.println( Header.readInt( bytes, 0) == -1);
    
    Header.writeInt( Integer.MAX_VALUE, bytes, 0);
    System.out.println( Header.readInt( bytes, 0) == Integer.MAX_VALUE);
    
    Header.writeInt( -Integer.MAX_VALUE, bytes, 0);
    System.out.println( Header.readInt( bytes, 0) == -Integer.MAX_VALUE);
  }
}
