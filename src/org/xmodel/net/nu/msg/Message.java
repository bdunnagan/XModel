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
      writeByte( type);
    }
    else if ( length < 256)
    {
      bytes = new byte[ 2 + length];
      writeByte( (byte)(type | 0x40));
      writeByte( (byte)length);
    }
    else if ( length < 65536)
    {
      bytes = new byte[ 3 + length];
      writeByte( (byte)(type | 0x80));
      writeShort( (short)length);
    }
    else
    {
      bytes = new byte[ 4 + length];
      writeByte( (byte)(type | 0xC0));
      writeInt( length);
    }
    
    write();
  }
  
  /**
   * Deserialize this message.
   */
  public final void deserialize()
  {
    type = (byte)(bytes[ 0] & 0x3F);
    byte mask = (byte)(bytes[ 0] & 0xC0);
    offset = 1;
    if ( mask == 0)
    {
      length = 0;
    }
    else if ( mask == 0x40)
    {
      length = readByte();
    }
    else if ( mask == 0x80)
    {
      length = readShort();
    }
    else
    {
      length = readInt();
    }
    
    read();
  }

  /**
   * Read this message from the underlying buffer.
   */
  protected abstract void read();
  
  /**
   * Write this message to the underlying buffer.
   */
  protected abstract void write();
  
  /**
   * Read a byte value from the message.
   * @return Returns the value.
   */
  protected final byte readByte()
  {
    return bytes[ offset++];
  }
  
  /**
   * Read a short value from the message.
   * @return Returns the value.
   */
  protected final short readShort()
  {
    short value = (short)(bytes[ offset++] << 8);
    return (short)(value + bytes[ offset++]);
  }
  
  /**
   * Read an integer value from the message.
   * @return Returns the value.
   */
  protected final int readInt()
  {
    int value = bytes[ offset++] << 24;
    value += bytes[ offset++] << 16;
    value += bytes[ offset++] << 8;
    return value + bytes[ offset++];
  }
  
  /**
   * Read an String from the message.
   * @return Returns the string.
   */
  protected final String readString()
  {
    int length = readInt();
    String string = new String( bytes, offset, length);
    offset += length;
    return string;
  }
  
  /**
   * Read bytes from teh stream.
   * @return Returns the bytes.
   */
  protected final byte[] readBytes()
  {
    int length = readInt();
    byte[] array = new byte[ length];
    System.arraycopy( bytes, offset, array, 0, length);
    offset += length;
    return array;
  }
  
  /**
   * Write a byte value into the message.
   * @param value The value.
   */
  protected final void writeByte( byte value)
  {
    bytes[ offset++] = value;
  }
  
  /**
   * Write a short value into the message.
   * @param value The value.
   */
  protected final void writeShort( short value)
  {
    bytes[ offset++] = (byte)((value >> 8) & 0xFF);
    bytes[ offset++] = (byte)((value) & 0xFF);
  }
  
  /**
   * Write an integer value into the message.
   * @param value The value.
   */
  protected final void writeInt( int value)
  {
    bytes[ offset++] = (byte)((value >> 24) & 0xFF);
    bytes[ offset++] = (byte)((value >> 16) & 0xFF);
    bytes[ offset++] = (byte)((value >> 8) & 0xFF);
    bytes[ offset++] = (byte)((value) & 0xFF);
  }
  
  /**
   * Write a String into the message.
   * @param string The string.
   */
  protected final void writeString( String string)
  {
    writeInt( string.length());
    byte[] array = string.getBytes();
    writeBytes( array, 0, array.length);
  }

  /**
   * Write the specified bytes into the message.
   * @param bytes The bytes.
   * @param offset The offset.
   * @param length The length.
   */
  protected final void writeBytes( byte[] bytes, int offset, int length)
  {
    writeInt( length);
    System.arraycopy( bytes, offset, this.bytes, this.offset, length);
    this.offset += length;
  }
  
  private byte type;
  private int length;
  private byte[] bytes;
  private int offset;
}
