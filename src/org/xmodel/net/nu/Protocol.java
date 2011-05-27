package org.xmodel.net.nu;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.xmodel.IModelObject;
import org.xmodel.compress.ICompressor;

/**
 * The protocol class for the NetworkCachingPolicy protocol.
 */
public abstract class Protocol implements INetFramer, INetReceiver
{
  public final static int version = 1;
  
  public enum Type
  {
    version,
    error,
    attachRequest,
    attachResponse,
    detachRequest,
    syncRequest,
    addChild,
    removeChild,
    changeAttribute,
    clearAttribute
  }

  protected Protocol( ICompressor compressor)
  {
    this.compressor = compressor;
    
    // allocate less than standard mtu
    buffer = ByteBuffer.allocateDirect( 4096);
    buffer.order( ByteOrder.BIG_ENDIAN);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.INetFramer#frame(java.nio.ByteBuffer)
   */
  @Override
  public final int frame( ByteBuffer buffer)
  {
    Type type = readMessageType( buffer);
    if ( type == Type.version) return 2;
    return readMessageLength( buffer);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.INetReceiver#receive(org.xmodel.net.nu.INetSender, java.nio.ByteBuffer)
   */
  @Override
  public final void receive( INetSender sender, ByteBuffer buffer)
  {
    Type type = readMessageType( buffer);

    // handle message types that don't have a length field
    switch( type)
    {
      case version: handleVersion( sender, buffer); return;
    }

    // handle message types that have a length field
    readMessageLength( buffer);
    switch( type)
    {
      case error:           handleError( sender, buffer); return;
      case attachRequest:   handleAttachRequest( sender, buffer); return;
      case attachResponse:  handleAttachResponse( sender, buffer); return;
      case detachRequest:   handleDetachRequest( sender, buffer); return;
      case syncRequest:     handleSyncRequest( sender, buffer); return;
      case addChild:        handleAddChild( sender, buffer); return;
      case removeChild:     handleRemoveChild( sender, buffer); return;
      case changeAttribute: handleChangeAttribute( sender, buffer); return;
      case clearAttribute:  handleClearAttribute( sender, buffer); return;
    }
  }

  /**
   * Send the protocol version.
   * @param sender The sender.
   * @param version The version.
   */
  public final void sendVersion( INetSender sender, short version)
  {
    initialize( buffer);
    buffer.putShort( version);
    finalize( buffer, Type.version, 2);
    sender.send( buffer);
  }
  
   /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleVersion( INetSender sender, ByteBuffer buffer)
  {
    int version = buffer.getShort();
    if ( version != Protocol.version)
    {
      buffer.put( (byte)Type.error.ordinal());
      writeString( this.buffer, String.format( "Expected protocol version %d", Protocol.version));
      sender.send( this.buffer);
      sender.close();
    }
  }
  
  /**
   * Send an error message.
   * @param sender The sender.
   * @param message The error message.
   */
  public final void sendError( INetSender sender, String message)
  {
    initialize( buffer);
    byte[] bytes = message.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.error, bytes.length);
    sender.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleError( INetSender sender, ByteBuffer buffer)
  {
    byte[] bytes = new byte[ buffer.remaining()];
    buffer.get( bytes);
    handleError( sender, new String( bytes));
  }
  
  /**
   * Handle an error message.
   * @param sender The sender.
   * @param message The message.
   */
  protected void handleError( INetSender sender, String message)
  {
  }
  
  /**
   * Send an attach request message.
   * @param sender The sender.
   * @param xpath The xpath.
   */
  public final void sendAttachRequeset( INetSender sender, String xpath)
  {
    initialize( buffer);
    byte[] bytes = xpath.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.attachRequest, bytes.length);
    sender.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleAttachRequest( INetSender sender, ByteBuffer buffer)
  {
    byte[] bytes = new byte[ buffer.remaining()];
    buffer.get( bytes);
    handleAttachRequest( sender, new String( bytes));
  }
  
  /**
   * Handle an attach request.
   * @param sender The sender.
   * @param xpath The xpath.
   */
  protected void handleAttachRequest( INetSender sender, String xpath)
  {
  }

  /**
   * Send an attach response message.
   * @param sender The sender.
   * @param xpath The xpath.
   * @param element The element.
   */
  public final void sendAttachResponse( INetSender sender, String xpath, IModelObject element)
  {
    initialize( buffer);
    int length = writeString( buffer, xpath);
    length += writeElement( buffer, element);
    finalize( buffer, Type.attachResponse, length);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleAttachResponse( INetSender sender, ByteBuffer buffer)
  {
    String xpath = readString( buffer);
    IModelObject element = readElement( buffer);
    handleAttachResponse( sender, xpath, element);
  }
  
  /**
   * Handle an attach response.
   * @param sender The sender.
   * @param xpath The xpath.
   * @param element The element.
   */
  protected void handleAttachResponse( INetSender sender, String xpath, IModelObject element)
  {
  }
  
  /**
   * Send an detach request message.
   * @param sender The sender.
   * @param xpath The xpath.
   */
  public final void sendDetachRequest( INetSender sender, String xpath)
  {
    initialize( buffer);
    byte[] bytes = xpath.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.detachRequest, bytes.length);
    sender.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleDetachRequest( INetSender sender, ByteBuffer buffer)
  {
  }
  
  /**
   * Handle an attach request.
   * @param sender The sender.
   * @param xpath The xpath.
   */
  protected void handleDetachRequest( INetSender sender, String xpath)
  {
  }
  
  /**
   * Send an sync request message.
   * @param sender The sender.
   * @param xpath The xpath.
   */
  public final void sendSyncRequest( INetSender sender, String xpath)
  {
    initialize( buffer);
    byte[] bytes = xpath.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.syncRequest, bytes.length);
    sender.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleSyncRequest( INetSender sender, ByteBuffer buffer)
  {
    String key = readString( buffer);
    handleSyncRequest( sender, key);
  }
  
  /**
   * Handle an attach request.
   * @param sender The sender.
   * @param key The reference key.
   */
  protected void handleSyncRequest( INetSender sender, String key)
  {
  }
  
  /**
   * Send an add child message.
   * @param sender The sender.
   * @param xpath The xpath.
   * @param element The element.
   * @param index The insertion index.
   */
  public final void sendAddChild( INetSender sender, String xpath, IModelObject element, int index)
  {
    initialize( buffer);
    int length = writeString( buffer, xpath);
    length += writeElement( buffer, element);
    buffer.putInt( index); length += 4;
    finalize( buffer, Type.addChild, length);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleAddChild( INetSender sender, ByteBuffer buffer)
  {
    String xpath = readString( buffer);
    IModelObject element = readElement( buffer);
    int index = buffer.getInt();
    handleAddChild( sender, xpath, element, index);
  }
  
  /**
   * Handle an add child message.
   * @param sender The sender.
   * @param xpath The path from the root to the parent.
   * @param child The child that was added.
   * @param index The insertion index.
   */
  protected void handleAddChild( INetSender sender, String xpath, IModelObject child, int index)
  {
  }
  
  /**
   * Send an add child message.
   * @param sender The sender.
   * @param xpath The xpath.
   * @param index The insertion index.
   */
  public final void sendRemoveChild( INetSender sender, String xpath, int index)
  {
    initialize( buffer);
    int length = writeString( buffer, xpath);
    buffer.putInt( index); length += 4;
    finalize( buffer, Type.removeChild, length);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleRemoveChild( INetSender sender, ByteBuffer buffer)
  {
    String xpath = readString( buffer);
    int index = buffer.getInt();
    handleRemoveChild( sender, xpath, index);
  }
  
  /**
   * Handle an remove child message.
   * @param sender The sender.
   * @param xpath The path from the root to the parent.
   * @param index The insertion index.
   */
  protected void handleRemoveChild( INetSender sender, String xpath, int index)
  {
  }
  
  /**
   * Send an change attribute message.
   * @param sender The sender.
   * @param xpath The xpath.
   * @param attrName The name of the attribute.
   * @param attrValue The new value.
   */
  public final void sendChangeAttribute( INetSender sender, String xpath, String attrName, byte[] value)
  {
    initialize( buffer);
    int length = writeString( buffer, xpath);
    length += writeString( buffer, attrName);
    length += writeBytes( buffer, value, 0, value.length, true);
    finalize( buffer, Type.changeAttribute, length);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleChangeAttribute( INetSender sender, ByteBuffer buffer)
  {
    String xpath = readString( buffer);
    String attrName = readString( buffer);
    byte[] attrValue = readBytes( buffer, true);
    handleChangeAttribute( sender, xpath, attrName, attrValue);
  }
  
  /**
   * Handle an attribute change message.
   * @param sender The sender.
   * @param xpath The xpath from the root to the element.
   * @param attrName The name of the attribute.
   * @param attrValue The attribute value.
   */
  protected void handleChangeAttribute( INetSender sender, String xpath, String attrName, byte[] attrValue)
  {
  }
  
  /**
   * Send a clear attribute message.
   * @param sender The sender.
   * @param xpath The xpath.
   * @param attrName The name of the attribute.
   */
  public final void sendChangeAttribute( INetSender sender, String xpath, String attrName)
  {
    initialize( buffer);
    int length = writeString( buffer, xpath);
    length += writeString( buffer, attrName);
    finalize( buffer, Type.clearAttribute, length);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleClearAttribute( INetSender sender, ByteBuffer buffer)
  {
    String xpath = readString( buffer);
    String attrName = readString( buffer);
    handleClearAttribute( sender, xpath, attrName);
  }
  
  /**
   * Handle an attribute change message.
   * @param sender The sender.
   * @param xpath The xpath from the root to the element.
   * @param attrName The name of the attribute.
   */
  protected void handleClearAttribute( INetSender sender, String xpath, String attrName)
  {
  }

  /**
   * Reserve the maximum amount of space for the header.
   * @param buffer The buffer.
   */
  public static void initialize( ByteBuffer buffer)
  {
    buffer.position( 5);
  }
  
  /**
   * Read the message type from the buffer.
   * @param buffer The buffer.
   * @return Returns the message type.
   */
  public static Type readMessageType( ByteBuffer buffer)
  {
    return Type.values()[ buffer.get() & 0x7f];
  }
  
  /**
   * Read the message length from the buffer.
   * @param buffer The buffer.
   * @return Returns the message length.
   */
  public static int readMessageLength( ByteBuffer buffer)
  {
    int mask = buffer.get( buffer.position() - 1) & 0x80;
    if ( mask == 0) return buffer.get();
    return buffer.getInt();
  }
  
  /**
   * Finalize the message by writing the header and preparing the buffer to be read.
   * @param buffer The buffer.
   * @param type The message type.
   * @param length The message length.
   */
  public static void finalize( ByteBuffer buffer, Type type, int length)
  {
    buffer.limit( buffer.position());
    if ( length < 128)
    {
      buffer.position( 3);
      buffer.put( (byte)type.ordinal());
      buffer.put( (byte)length);
    }
    else
    {
      buffer.position( 1);
      buffer.put( (byte)(type.ordinal() | 0x80));
      buffer.putInt( length);
    }
  }
  
  /**
   * Read a length from the buffer.
   * @param buffer The buffer.
   * @param small True means 1 or 4 bytes. False means 2 or 4 bytes.
   * @return Returns the length.
   */
  private static int readLength( ByteBuffer buffer, boolean small)
  {
    buffer.mark();
    if ( small)
    {
      int length = buffer.get();
      if ( length >= 0) return length;
    }
    else
    {
      int length = buffer.getShort();
      if ( length >= 0) return length;
    }
    
    buffer.reset();
    return -buffer.getInt();
  }
  
  /**
   * Write a length into the buffer in either one, two or four bytes.
   * @param buffer The buffer.
   * @param length The length.
   * @param small True means 1 or 4 bytes. False means 2 or 4 bytes.
   */
  private static int writeLength( ByteBuffer buffer, int length, boolean small)
  {
    if ( small)
    {
      if ( length < 128)
      {
        buffer.put( (byte)length);
        return 1;
      }
    }
    else
    {
      if ( length < 32768)
      {
        buffer.putShort( (short)length);
        return 2;
      }
    }
    
    buffer.putInt( -length);
    return 4;
  }
  
  /**
   * Read a block of bytes from the message.
   * @param buffer The buffer.
   * @return Returns the bytes read.
   */
  public static byte[] readBytes( ByteBuffer buffer, boolean small)
  {
    int length = readLength( buffer, small);
    byte[] bytes = new byte[ length];
    buffer.get( bytes, 0, length);
    return bytes;
  }
  
  /**
   * Write a block of bytes to the message.
   * @param buffer The buffer.
   * @param bytes The bytes.
   * @param offset The offset.
   * @param length The length.
   */
  public static int writeBytes( ByteBuffer buffer, byte[] bytes, int offset, int length, boolean small)
  {
    int prefix = writeLength( buffer, length, small);
    buffer.put( bytes, offset, length);
    return prefix + length;
  }
  
  /**
   * Read an String from the message.
   * @param buffer The buffer.
   * @return Returns the string.
   */
  public static String readString( ByteBuffer buffer)
  {
    return new String( readBytes( buffer, true));
  }
  
  /**
   * Write a String into the message.
   * @param buffer The buffer.
   * @param string The string.
   */
  public static int writeString( ByteBuffer buffer, String string)
  {
    byte[] bytes = string.getBytes();
    return writeBytes( buffer, bytes, 0, bytes.length, true);
  }
  
  /**
   * Read an IModelObject from the message.
   * @param buffer The buffer.
   * @return Returns the element.
   */
  public IModelObject readElement( ByteBuffer buffer)
  {
    byte[] bytes = readBytes( buffer, false);
    return compressor.decompress( bytes, 0);
  }

  /**
   * Write an IModelObject to the message.
   * @param buffer The buffer.
   * @param element The element.
   */
  public int writeElement( ByteBuffer buffer, IModelObject element)
  {
    byte[] bytes = compressor.compress( element);
    return writeBytes( buffer, bytes, 0, bytes.length, false);
  }

  private ByteBuffer buffer;
  private ICompressor compressor;
}
