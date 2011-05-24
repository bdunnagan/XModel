package org.xmodel.net.nu.msg;

public class AddChildMessage extends Message
{
  public final static byte type = 5;
  
  public AddChildMessage( String xpath, byte[] child, int index)
  {
    super( type);
    setLength( xpath.length() + child.length + 4);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#serialize(byte[], int)
   */
  @Override
  protected void serialize( byte[] bytes, int offset)
  {
    byte[] temp = xpath.getBytes();
    System.arraycopy( temp, 0, bytes, offset, temp.length);
    offset += temp.length;
    
    System.arraycopy( child, 0, bytes, offset, child.length);
    offset += child.length;
    
    bytes[ offset++] = (byte)((index >> 24) & 0xFF);
    bytes[ offset++] = (byte)((index >> 16) & 0xFF);
    bytes[ offset++] = (byte)((index >> 8) & 0xFF);
    bytes[ offset] = (byte)((index) & 0xFF);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#deserialize(byte[], int)
   */
  @Override
  protected void deserialize( byte[] bytes, int offset)
  {
    xpath = new String( bytes, offset, 
  }
  
  private String xpath;
  private byte[] child;
  private int index;
}
