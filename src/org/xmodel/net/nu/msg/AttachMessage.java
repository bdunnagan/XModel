package org.xmodel.net.nu.msg;

public class AttachMessage extends Message
{
  public final static byte type = 1;
  
  public AttachMessage( String xpath)
  {
    super( type);
    setLength( xpath.length() + 4);
  }

  /**
   * @return Returns the xpath.
   */
  public String getXPath()
  {
    return xpath;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#read()
   */
  @Override
  protected void read()
  {
    xpath = readString();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#write()
   */
  @Override
  protected void write()
  {
    writeString( xpath);
  }
  
  private String xpath;
}
