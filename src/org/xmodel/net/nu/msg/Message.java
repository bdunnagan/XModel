package org.xmodel.net.nu.msg;

public abstract class Message
{
  protected Message( byte type)
  {
    this.type = type;
  }
  
  /**
   * @return Returns the message type.
   */
  public byte getType()
  {
    return type;
  }
  
  /**
   * @return Returns the length of the message.
   */
  public int getLength()
  {
    return length;
  }
  
  /**
   * Set the length of the message.
   * @param length The length.
   */
  protected void setLength( int length)
  {
    this.length = length;
  }
  
  /**
   * Serialize this message.
   */
  public final void serialize()
  {
    if ( length == 0)
    {
      bytes = new byte[ 1];
      bytes[ 0] = type;
      serialize( bytes, 1);
    }
    else if ( length < 256)
    {
      bytes = new byte[ 2 + length];
      bytes[ 0] = (byte)(type | 0x40);
      bytes[ 1] = (byte)length;
      serialize( bytes, 2);
    }
    else if ( length < 65536)
    {
      bytes = new byte[ 3 + length];
      bytes[ 0] = (byte)(type | 0x80);
      bytes[ 1] = (byte)((length >> 8) & 0xFF);
      bytes[ 2] = (byte)((length) & 0xFF);
      serialize( bytes, 3);
    }
    else
    {
      bytes = new byte[ 4 + length];
      bytes[ 0] = (byte)(type | 0xC0);
      bytes[ 1] = (byte)((length >> 24) & 0xFF);
      bytes[ 2] = (byte)((length >> 16) & 0xFF);
      bytes[ 3] = (byte)((length >> 8) & 0xFF);
      bytes[ 4] = (byte)((length) & 0xFF);
      serialize( bytes, 4);
    }
  }
  
  /**
   * Deserialize this message.
   */
  public final void deserialize()
  {
    type = (byte)(bytes[ 0] & 0x3F);
    
    byte mask = (byte)(bytes[ 0] & 0xC0);
    if ( mask == 0)
    {
      length = 0;
      deserialize( bytes, 1);
    }
    else if ( mask == 0x40)
    {
      length = bytes[ 1];
      deserialize( bytes, 2);
    }
    else if ( mask == 0x80)
    {
      length = bytes[ 1] << 8;
      length += bytes[ 2];
      deserialize( bytes, 3);
    }
    else
    {
      length = bytes[ 1] << 24;
      length += bytes[ 2] << 16;
      length += bytes[ 3] << 8;
      length += bytes[ 4];
      deserialize( bytes, 4);
    }
  }
  
  /**
   * Serialize the content of this message.
   * @param bytes The byte array.
   * @param offset The starting offset.
   */
  protected abstract void serialize( byte[] bytes, int offset);
  
  /**
   * Deserialize the content of this message.
   * @param bytes The byte array.
   * @param offset The starting offset.
   */
  protected abstract void deserialize( byte[] bytes, int offset);
  
  private byte type;
  private int length;
  private byte[] bytes;
}
