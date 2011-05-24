package org.xmodel.net.nu.msg;

public class DetachMessage extends Message
{
  public final static byte type = 2;
  
  public DetachMessage( String xpath)
  {
    super( type);
    setLength( xpath.length());
  }
  
  /**
   * @return Returns the xpath.
   */
  public final String getXPath()
  {
    return xpath;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#serialize(byte[], int)
   */
  @Override
  protected void serialize( byte[] bytes, int offset)
  {
    byte[] content = xpath.getBytes();
    System.arraycopy( content, 0, bytes, offset, content.length);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#deserialize(byte[], int)
   */
  @Override
  protected void deserialize( byte[] bytes, int offset)
  {
    xpath = new String( bytes, offset, bytes.length - offset);
  }
  
  private String xpath;
}
