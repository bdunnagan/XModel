package org.xmodel.net.nu.msg;

public class BytesMessage extends Message
{
  public BytesMessage( byte type, byte[] bytes)
  {
    super( type);
    setLength( bytes.length);
  }
  
  /**
   * @return Returns the bytes.
   */
  public final byte[] get()
  {
    return bytes;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#serialize(byte[], int)
   */
  @Override
  protected void serialize( byte[] bytes, int offset)
  {
    System.arraycopy( bytes, 0, bytes, offset, bytes.length);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#deserialize(byte[], int)
   */
  @Override
  protected void deserialize( byte[] bytes, int offset)
  {
    bytes = new byte[ getLength()];
    System.arraycopy( bytes, offset, bytes, 0, bytes.length);
  }
  
  private byte[] bytes;
}
