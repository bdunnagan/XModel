package org.xmodel.net.nu.msg;

public class AttachResponse extends Message
{
  public final static byte type = 4;
  
  public AttachResponse()
  {
    super( type);
  }
  
  public AttachResponse( byte[] content)
  {
    super( type);
    setLength( 4 + content.length);
  }

  /**
   * @return Returns the content.
   */
  public byte[] getContent()
  {
    return content;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#read()
   */
  @Override
  protected void read()
  {
    content = readBytes();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#write()
   */
  @Override
  protected void write()
  {
    writeBytes( content, 0, content.length);
  }

  private byte[] content;
}
