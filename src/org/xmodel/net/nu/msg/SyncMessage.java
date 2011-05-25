package org.xmodel.net.nu.msg;

public class SyncMessage extends Message
{
  public final static byte type = 3;
  
  public SyncMessage( String key)
  {
    super( type);
    setLength( key.length() + 4);
  }

  /**
   * @return Returns the key.
   */
  public String getKey()
  {
    return key;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#read()
   */
  @Override
  protected void read()
  {
    key = readString();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#write()
   */
  @Override
  protected void write()
  {
    writeString( key);
  }
  
  private String key;
}
