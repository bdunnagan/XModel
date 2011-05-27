package org.xmodel.net.nu;

import java.nio.ByteBuffer;

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
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.INetFramer#frame(java.nio.ByteBuffer)
   */
  @Override
  public final int frame( ByteBuffer buffer)
  {
    byte first = buffer.get();
    int type = first & 0x7f;
    if ( Type.values()[ type] == Type.version) return 2;
    return getLength( first, buffer);
  }
  
  /**
   * Returns the length field at the start of the buffer.
   * @param first The first byte of the message.
   * @param buffer The buffer.
   * @return Returns the length field at the start of the buffer.
   */
  private final int getLength( byte first, ByteBuffer buffer)
  {
    if ( (first >> 7) == 0)
    {
      return buffer.get();
    }
    else
    {
      return buffer.getInt();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.INetReceiver#receive(org.xmodel.net.nu.INetSender, java.nio.ByteBuffer)
   */
  @Override
  public final void receive( INetSender sender, ByteBuffer buffer)
  {
    byte first = buffer.get();
    Type type = Type.values()[ first & 0x7f];
    
    // no need to reread length
    int skip = getLength( first, buffer);
    buffer.position( skip + 1);
    
    switch( type)
    {
      case version:         handleVersion( sender, buffer); break;
      case error:           handleError( sender, buffer); break;
      case attachRequest:   handleAttachRequest( sender, buffer); break;
      case attachResponse:  handleAttachResponse( sender, buffer); break;
      case detachRequest:   handleDetachRequest( sender, buffer); break;
      case syncRequest:     handleSyncRequest( sender, buffer); break;
      case addChild:        handleAddChild( sender, buffer); break;
      case removeChild:     handleRemoveChild( sender, buffer); break;
      case changeAttribute: handleChangeAttribute( sender, buffer); break;
      case clearAttribute:  handleClearAttribute( sender, buffer); break;
    }
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
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleError( INetSender sender, ByteBuffer buffer)
  {
    String message = readString( buffer);
    handleError( sender, message);
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
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleAttachRequest( INetSender sender, ByteBuffer buffer)
  {
    String xpath = readString( buffer);
    handleAttachRequest( sender, xpath);
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
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleDetachRequest( INetSender sender, ByteBuffer buffer)
  {
    String xpath = readString( buffer);
    handleDetachRequest( sender, xpath);
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
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleChangeAttribute( INetSender sender, ByteBuffer buffer)
  {
    String xpath = readString( buffer);
    String attrName = readString( buffer);
    byte[] attrValue = readBytes( buffer);
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
   * Read a block of bytes from the message.
   * @param buffer The buffer.
   * @return Returns the bytes read.
   */
  public static byte[] readBytes( ByteBuffer buffer)
  {
    int length = buffer.getInt();
    byte[] bytes = new byte[ length];
    buffer.get( bytes, 0, length);
    return bytes;
  }
  
  /**
   * Read an String from the message.
   * @param buffer The buffer.
   * @return Returns the string.
   */
  public static String readString( ByteBuffer buffer)
  {
    return new String( readBytes( buffer));
  }
  
  /**
   * Read an IModelObject from the message.
   * @param buffer The buffer.
   * @return Returns the element.
   */
  public IModelObject readElement( ByteBuffer buffer)
  {
    byte[] bytes = readBytes( buffer);
    return compressor.decompress( bytes, 0);
  }
  
  /**
   * Write a block of bytes to the message.
   * @param buffer The buffer.
   * @param bytes The bytes.
   * @param offset The offset.
   * @param length The length.
   */
  public static void writeBytes( ByteBuffer buffer, byte[] bytes, int offset, int length)
  {
    buffer.putInt( length);
    buffer.put( bytes, offset, length);
  }
  
  /**
   * Write a String into the message.
   * @param buffer The buffer.
   * @param string The string.
   */
  public static void writeString( ByteBuffer buffer, String string)
  {
    byte[] bytes = string.getBytes();
    writeBytes( buffer, bytes, 0, bytes.length);
  }
  
  /**
   * Write an IModelObject to the message.
   * @param buffer The buffer.
   * @param element The element.
   */
  public void writeElement( ByteBuffer buffer, IModelObject element)
  {
    byte[] bytes = compressor.compress( element);
    writeBytes( buffer, bytes, 0, bytes.length);
  }

  private ByteBuffer buffer;
  private ICompressor compressor;
}
