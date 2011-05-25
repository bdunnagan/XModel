package org.xmodel.net.nu.msg;

public class ErrorResponse extends Message
{
  public final static byte type = 0x3f;
  
  public ErrorResponse( String message)
  {
    super( type);
    setLength( message.length() + 4);
  }

  /**
   * @return Returns the error message.
   */
  public String getMessage()
  {
    return message;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#read()
   */
  @Override
  protected void read()
  {
    message = readString();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#write()
   */
  @Override
  protected void write()
  {
    writeString( message);
  }
  
  private String message;
}
