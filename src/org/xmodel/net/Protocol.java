package org.xmodel.net;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.xmodel.IModelObject;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.compress.TabularCompressor.PostCompression;

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
    clearAttribute,
    queryRequest,
    queryResponse,
    debugStepIn,
    debugStepOver,
    debugStepOut
  }

  protected Protocol()
  {
    this.compressor = new TabularCompressor( PostCompression.zip);
    this.timeout = 10000;
    
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
    buffer.get();
    return readMessageLength( buffer);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.INetReceiver#receive(org.xmodel.net.nu.INetSender, java.nio.ByteBuffer)
   */
  @Override
  public final void receive( INetSender sender, ByteBuffer buffer)
  {
    Type type = readMessageType( buffer);
    readMessageLength( buffer);
    switch( type)
    {
      case version:         handleVersion( sender, buffer); return;
      case error:           handleError( sender, buffer); return;
      case attachRequest:   handleAttachRequest( sender, buffer); return;
      case attachResponse:  handleAttachResponse( sender, buffer); return;
      case detachRequest:   handleDetachRequest( sender, buffer); return;
      case syncRequest:     handleSyncRequest( sender, buffer); return;
      case addChild:        handleAddChild( sender, buffer); return;
      case removeChild:     handleRemoveChild( sender, buffer); return;
      case changeAttribute: handleChangeAttribute( sender, buffer); return;
      case clearAttribute:  handleClearAttribute( sender, buffer); return;
      case queryRequest:    handleQueryRequest( sender, buffer); return;
      case queryResponse:   handleQueryResponse( sender, buffer); return;
      case debugStepIn:     handleDebugStepIn( sender, buffer); return;
      case debugStepOver:   handleDebugStepOver( sender, buffer); return;
      case debugStepOut:    handleDebugStepOut( sender, buffer); return;
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
  public final void sendAttachRequest( INetSender sender, String xpath)
  {
    initialize( buffer);
    byte[] bytes = xpath.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.attachRequest, bytes.length);
    sender.send( buffer, timeout);
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
   * @param element The element.
   */
  public final void sendAttachResponse( INetSender sender, IModelObject element)
  {
    initialize( buffer);
    byte[] bytes = compressor.compress( element);
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.attachResponse, bytes.length);
    sender.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleAttachResponse( INetSender sender, ByteBuffer buffer)
  {
    byte[] bytes = new byte[ buffer.remaining()];
    buffer.get( bytes);
    IModelObject element = compressor.decompress( bytes, 0);
    handleAttachResponse( sender, element);
  }
  
  /**
   * Handle an attach response.
   * @param sender The sender.
   * @param element The element.
   */
  protected void handleAttachResponse( INetSender sender, IModelObject element)
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
   * @param key The key.
   */
  public final void sendSyncRequest( INetSender sender, String key)
  {
    initialize( buffer);
    byte[] bytes = key.getBytes();
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
    byte[] bytes = new byte[ buffer.remaining()];
    buffer.get( bytes);
    handleSyncRequest( sender, new String( bytes));
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
    sender.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleAddChild( INetSender sender, ByteBuffer buffer)
  {
    String xpath = readString( buffer);
    byte[] bytes = readBytes( buffer, false);
    int index = buffer.getInt();
    handleAddChild( sender, xpath, bytes, index);
  }
  
  /**
   * Handle an add child message.
   * @param sender The sender.
   * @param xpath The path from the root to the parent.
   * @param child The child that was added as a compressed byte array.
   * @param index The insertion index.
   */
  protected void handleAddChild( INetSender sender, String xpath, byte[] child, int index)
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
    sender.send( buffer);
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
  public final void sendChangeAttribute( INetSender sender, String xpath, String attrName, Object value)
  {
    byte[] bytes = serialize( value);
    
    initialize( buffer);
    int length = writeString( buffer, xpath);
    length += writeString( buffer, attrName);
    length += writeBytes( buffer, bytes, 0, bytes.length, true);
    finalize( buffer, Type.changeAttribute, length);
    sender.send( buffer);
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
    handleChangeAttribute( sender, xpath, attrName, deserialize( attrValue));
  }
  
  /**
   * Handle an attribute change message.
   * @param sender The sender.
   * @param xpath The xpath from the root to the element.
   * @param attrName The name of the attribute.
   * @param attrValue The attribute value.
   */
  protected void handleChangeAttribute( INetSender sender, String xpath, String attrName, Object attrValue)
  {
  }
  
  /**
   * Send a clear attribute message.
   * @param sender The sender.
   * @param xpath The xpath.
   * @param attrName The name of the attribute.
   */
  public final void sendClearAttribute( INetSender sender, String xpath, String attrName)
  {
    initialize( buffer);
    int length = writeString( buffer, xpath);
    length += writeString( buffer, attrName);
    finalize( buffer, Type.clearAttribute, length);
    sender.send( buffer);
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
   * Send a query request message.
   * @param sender The sender.
   * @param xpath The xpath.
   */
  public final void sendQueryRequest( INetSender sender, String xpath)
  {
    initialize( buffer);
    byte[] bytes = xpath.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.queryRequest, bytes.length);
    sender.send( buffer, timeout);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleQueryRequest( INetSender sender, ByteBuffer buffer)
  {
    byte[] bytes = new byte[ buffer.remaining()];
    buffer.get( bytes);
    handleQueryRequest( sender, new String( bytes));
  }
  
  /**
   * Handle a query requset.
   * @param sender The sender.
   * @param xpath The query.
   */
  protected void handleQueryRequest( INetSender sender, String xpath)
  {
  }
  
  /**
   * Send a query response message.
   * @param sender The sender.
   * @param result The result.
   */
  public final void sendQueryResponse( INetSender sender, Object result)
  {
    initialize( buffer);
    byte[] bytes = serialize( result);
    buffer.put( bytes);
    finalize( buffer, Type.queryResponse, bytes.length);
    sender.send( buffer, timeout);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleQueryResponse( INetSender sender, ByteBuffer buffer)
  {
    byte[] bytes = new byte[ buffer.remaining()];
    buffer.get( bytes);
    handleQueryResponse( sender, bytes);
  }
  
  /**
   * Handle a query requset.
   * @param sender The sender.
   * @param bytes The serialized query result.
   */
  protected void handleQueryResponse( INetSender sender, byte[] bytes)
  {
  }
  
  /**
   * Send a debug step message.
   * @param sender The sender.
   */
  public final void sendDebugStepIn( INetSender sender)
  {
    initialize( buffer);
    finalize( buffer, Type.debugStepIn, 0);
    sender.send( buffer, 60000);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleDebugStepIn( INetSender sender, ByteBuffer buffer)
  {
    handleDebugStepIn( sender);
  }
  
  /**
   * Handle a debug step.
   * @param sender The sender.
   */
  protected void handleDebugStepIn( INetSender sender)
  {
  }
  
  /**
   * Send a debug step message.
   * @param sender The sender.
   */
  public final void sendDebugStepOver( INetSender sender)
  {
    initialize( buffer);
    finalize( buffer, Type.debugStepOver, 0);
    sender.send( buffer, 60000);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleDebugStepOver( INetSender sender, ByteBuffer buffer)
  {
    handleDebugStepOver( sender);
  }
  
  /**
   * Handle a debug step.
   * @param sender The sender.
   */
  protected void handleDebugStepOver( INetSender sender)
  {
  }
  
  /**
   * Send a debug step message.
   * @param sender The sender.
   */
  public final void sendDebugStepOut( INetSender sender)
  {
    initialize( buffer);
    finalize( buffer, Type.debugStepOut, 0);
    sender.send( buffer, 60000);
  }
  
  /**
   * Handle the specified message buffer.
   * @param sender The sender.
   * @param buffer The buffer.
   */
  private final void handleDebugStepOut( INetSender sender, ByteBuffer buffer)
  {
    handleDebugStepOut( sender);
  }
  
  /**
   * Handle a debug step.
   * @param sender The sender.
   */
  protected void handleDebugStepOut( INetSender sender)
  {
  }
  
  /**
   * Returns the serialized attribute value.
   * @param object The attribute value.
   * @return Returns the serialized attribute value.
   */
  private byte[] serialize( Object object)
  {
    if ( object == null) return new byte[ 0];
    
    // TODO: beef this up!
    return object.toString().getBytes();
  }
  
  /**
   * Returns the deserialized attribute value.
   * @param bytes The serialized attribute value.
   * @return Returns the deserialized attribute value.
   */
  private Object deserialize( byte[] bytes)
  {
    if ( bytes.length == 0) return null;
    
    // TODO: beef this up!
    return new String( bytes);
  }
  
  /**
   * Reserve the maximum amount of space for the header.
   * @param buffer The buffer.
   */
  public static void initialize( ByteBuffer buffer)
  {
    System.out.println( buffer);
    buffer.clear();
    buffer.position( 5);
  }
  
  /**
   * Read the message type from the buffer.
   * @param buffer The buffer.
   * @return Returns the message type.
   */
  public static Type readMessageType( ByteBuffer buffer)
  {
    int index = buffer.get() & 0x7f;
    return Type.values()[ index];
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
      buffer.put( 3, (byte)type.ordinal());
      buffer.put( 4, (byte)length);
      buffer.position( 3);
    }
    else
    {
      buffer.put( 0, (byte)(type.ordinal() | 0x80));
      buffer.putInt( 1, length);
      buffer.position( 0);
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

  protected ICompressor compressor;
  private ByteBuffer buffer;
  private int timeout;
}
