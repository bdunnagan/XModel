package org.xmodel.net.nu.msg;

public class VersionMessage extends Message
{
  public final static byte type = 0;
  
  public VersionMessage()
  {
    super( type);
  }
  
  public VersionMessage( int version)
  {
    super( type);
    this.version = version;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#read()
   */
  @Override
  protected void read()
  {
    version = readInt();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#write()
   */
  @Override
  protected void write()
  {
    writeInt( version);
  }

  private int version;
}
