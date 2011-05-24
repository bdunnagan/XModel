package org.xmodel.net.nu.msg;

public class StringMessage extends Message
{
  public StringMessage( byte type, String string)
  {
    super( type);
    setLength( string.length());
  }
  
  /**
   * @return Returns the string.
   */
  public final String get()
  {
    return string;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#serialize(byte[], int)
   */
  @Override
  protected void serialize( byte[] bytes, int offset)
  {
    byte[] content = string.getBytes();
    System.arraycopy( content, 0, bytes, offset, content.length);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#deserialize(byte[], int)
   */
  @Override
  protected void deserialize( byte[] bytes, int offset)
  {
    string = new String( bytes, offset, bytes.length - offset);
  }
  
  private String string;
}
