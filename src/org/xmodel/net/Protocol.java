package org.xmodel.net;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.xmodel.IModelObject;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.compress.TabularCompressor.PostCompression;
import org.xmodel.log.Log;
import org.xmodel.net.stream.Connection;
import org.xmodel.net.stream.ITcpListener;

/**
 * The protocol class for the NetworkCachingPolicy protocol.
 */
public abstract class Protocol implements ITcpListener
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
    this.responseQueue = new SynchronousQueue<ByteBuffer>();
    
    // allocate less than standard mtu
    buffer = ByteBuffer.allocate( 4096);
    buffer.order( ByteOrder.BIG_ENDIAN);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.stream.ITcpListener#onConnect(org.xmodel.net.stream.Connection)
   */
  @Override
  public void onConnect( Connection connection)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.stream.ITcpListener#onClose(org.xmodel.net.stream.Connection)
   */
  @Override
  public void onClose( Connection connection)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.stream.ITcpListener#onReceive(org.xmodel.net.stream.Connection, java.nio.ByteBuffer)
   */
  @Override
  public void onReceive( Connection connection, ByteBuffer buffer)
  {
    buffer.mark();
    while( handleMessage( connection, buffer)) buffer.mark();
    buffer.reset();
  }
  
  /**
   * Parse and handle one message from the specified buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @return Returns true if a message was handled.
   */
  private final boolean handleMessage( Connection connection, ByteBuffer buffer)
  {
    try
    {
      Type type = readMessageType( buffer);
      
      int length = readMessageLength( buffer);
      if ( length > buffer.remaining()) return false;
      
      switch( type)
      {
        case version:         handleVersion( connection, buffer, length); return true;
        case error:           handleError( connection, buffer, length); return true;
        case attachRequest:   handleAttachRequest( connection, buffer, length); return true;
        case attachResponse:  handleAttachResponse( connection, buffer, length); return true;
        case detachRequest:   handleDetachRequest( connection, buffer, length); return true;
        case syncRequest:     handleSyncRequest( connection, buffer, length); return true;
        case addChild:        handleAddChild( connection, buffer, length); return true;
        case removeChild:     handleRemoveChild( connection, buffer, length); return true;
        case changeAttribute: handleChangeAttribute( connection, buffer, length); return true;
        case clearAttribute:  handleClearAttribute( connection, buffer, length); return true;
        case queryRequest:    handleQueryRequest( connection, buffer, length); return true;
        case queryResponse:   handleQueryResponse( connection, buffer, length); return true;
        case debugStepIn:     handleDebugStepIn( connection, buffer, length); return true;
        case debugStepOver:   handleDebugStepOver( connection, buffer, length); return true;
        case debugStepOut:    handleDebugStepOut( connection, buffer, length); return true;
      }
    }
    catch( BufferUnderflowException e)
    {
    }
    
    return false;
  }
  
  /**
   * Send the protocol version.
   * @param connection The connection.
   * @param version The version.
   */
  public final void sendVersion( Connection connection, short version) throws IOException
  {
    log.debugf( "sendVersion: %d\n", version);
    initialize( buffer);
    buffer.putShort( version);
    finalize( buffer, Type.version, 2);
    connection.write( buffer);
  }
  
   /**
   * Handle the specified message buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleVersion( Connection connection, ByteBuffer buffer, int length)
  {
    int version = buffer.getShort();
    log.debugf( "handleVersion: %d\n", version);
    if ( version != Protocol.version)
    {
      connection.close();
    }
  }
  
  /**
   * Send an error message.
   * @param connection The connection.
   * @param message The error message.
   */
  public final void sendError( Connection connection, String message) throws IOException
  {
    log.debugf( "sendError: %s\n", message);
    initialize( buffer);
    byte[] bytes = message.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.error, bytes.length);
    connection.write( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleError( Connection connection, ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    log.debugf( "handleError: %s\n", new String( bytes));
    handleError( connection, new String( bytes));
  }
  
  /**
   * Handle an error message.
   * @param connection The connection.
   * @param message The message.
   */
  protected void handleError( Connection connection, String message)
  {
  }
  
  /**
   * Send an attach request message.
   * @param connection The connection.
   * @param xpath The xpath.
   */
  public final void sendAttachRequest( Connection connection, String xpath) throws IOException
  {
    log.debugf( "sendAttachRequest: %s\n", xpath);
    initialize( buffer);
    byte[] bytes = xpath.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.attachRequest, bytes.length);

    // send and wait for response
    ByteBuffer response = send( connection, buffer, timeout);
    IModelObject element = compressor.decompress( response.array(), response.arrayOffset() + response.position());
    log.debugf( "handleAttachRequest: %s\n", element.getType());
    handleAttachResponse( connection, element);
  }
  
  /**
   * Handle the specified message buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleAttachRequest( Connection connection, ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    log.debugf( "handleAttachRequest: %s\n", new String( bytes));
    handleAttachRequest( connection, new String( bytes));
  }
  
  /**
   * Handle an attach request.
   * @param connection The connection.
   * @param xpath The xpath.
   */
  protected void handleAttachRequest( Connection connection, String xpath)
  {
  }

  /**
   * Send an attach response message.
   * @param connection The connection.
   * @param element The element.
   */
  public final void sendAttachResponse( Connection connection, IModelObject element) throws IOException
  {
    log.debugf( "sendAttachResponse: %s\n", element.getType());
    initialize( buffer);
    byte[] bytes = compressor.compress( element);
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.attachResponse, bytes.length);
    connection.write( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleAttachResponse( Connection connection, ByteBuffer buffer, int length)
  {
    ByteBuffer response = ByteBuffer.allocate( length);
    buffer.get( response.array(), 0, length);
    response.limit( length);
    response.flip();
    try { responseQueue.put( response);} catch( InterruptedException e) {}
  }
  
  /**
   * Handle an attach response.
   * @param connection The connection.
   * @param element The element.
   */
  protected void handleAttachResponse( Connection connection, IModelObject element)
  {
  }
  
  /**
   * Send an detach request message.
   * @param connection The connection.
   * @param xpath The xpath.
   */
  public final void sendDetachRequest( Connection connection, String xpath) throws IOException
  {
    initialize( buffer);
    byte[] bytes = xpath.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.detachRequest, bytes.length);
    connection.write( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleDetachRequest( Connection connection, ByteBuffer buffer, int length)
  {
  }
  
  /**
   * Handle an attach request.
   * @param connection The connection.
   * @param xpath The xpath.
   */
  protected void handleDetachRequest( Connection connection, String xpath)
  {
  }
  
  /**
   * Send an sync request message.
   * @param connection The connection.
   * @param key The key.
   */
  public final void sendSyncRequest( Connection connection, String key) throws IOException
  {
    initialize( buffer);
    byte[] bytes = key.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.syncRequest, bytes.length);
    connection.write( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleSyncRequest( Connection connection, ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    handleSyncRequest( connection, new String( bytes));
  }
  
  /**
   * Handle an attach request.
   * @param connection The connection.
   * @param key The reference key.
   */
  protected void handleSyncRequest( Connection connection, String key)
  {
  }
  
  /**
   * Send an add child message.
   * @param connection The connection.
   * @param xpath The xpath.
   * @param element The element.
   * @param index The insertion index.
   */
  public final void sendAddChild( Connection connection, String xpath, IModelObject element, int index) throws IOException
  {
    initialize( buffer);
    int length = writeString( buffer, xpath);
    length += writeElement( buffer, element);
    buffer.putInt( index); length += 4;
    finalize( buffer, Type.addChild, length);
    connection.write( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleAddChild( Connection connection, ByteBuffer buffer, int length)
  {
    String xpath = readString( buffer);
    byte[] bytes = readBytes( buffer, false);
    int index = buffer.getInt();
    handleAddChild( connection, xpath, bytes, index);
  }
  
  /**
   * Handle an add child message.
   * @param connection The connection.
   * @param xpath The path from the root to the parent.
   * @param child The child that was added as a compressed byte array.
   * @param index The insertion index.
   */
  protected void handleAddChild( Connection connection, String xpath, byte[] child, int index)
  {
  }
  
  /**
   * Send an add child message.
   * @param connection The connection.
   * @param xpath The xpath.
   * @param index The insertion index.
   */
  public final void sendRemoveChild( Connection connection, String xpath, int index) throws IOException
  {
    initialize( buffer);
    int length = writeString( buffer, xpath);
    buffer.putInt( index); length += 4;
    finalize( buffer, Type.removeChild, length);
    connection.write( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleRemoveChild( Connection connection, ByteBuffer buffer, int length)
  {
    String xpath = readString( buffer);
    int index = buffer.getInt();
    handleRemoveChild( connection, xpath, index);
  }
  
  /**
   * Handle an remove child message.
   * @param connection The connection.
   * @param xpath The path from the root to the parent.
   * @param index The insertion index.
   */
  protected void handleRemoveChild( Connection connection, String xpath, int index)
  {
  }
  
  /**
   * Send an change attribute message.
   * @param connection The connection.
   * @param xpath The xpath.
   * @param attrName The name of the attribute.
   * @param attrValue The new value.
   */
  public final void sendChangeAttribute( Connection connection, String xpath, String attrName, Object value) throws IOException
  {
    byte[] bytes = serialize( value);
    
    initialize( buffer);
    int length = writeString( buffer, xpath);
    length += writeString( buffer, attrName);
    length += writeBytes( buffer, bytes, 0, bytes.length, true);
    finalize( buffer, Type.changeAttribute, length);
    connection.write( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleChangeAttribute( Connection connection, ByteBuffer buffer, int length)
  {
    String xpath = readString( buffer);
    String attrName = readString( buffer);
    byte[] attrValue = readBytes( buffer, true);
    handleChangeAttribute( connection, xpath, attrName, deserialize( attrValue));
  }
  
  /**
   * Handle an attribute change message.
   * @param connection The connection.
   * @param xpath The xpath from the root to the element.
   * @param attrName The name of the attribute.
   * @param attrValue The attribute value.
   */
  protected void handleChangeAttribute( Connection connection, String xpath, String attrName, Object attrValue)
  {
  }
  
  /**
   * Send a clear attribute message.
   * @param connection The connection.
   * @param xpath The xpath.
   * @param attrName The name of the attribute.
   */
  public final void sendClearAttribute( Connection connection, String xpath, String attrName) throws IOException
  {
    initialize( buffer);
    int length = writeString( buffer, xpath);
    length += writeString( buffer, attrName);
    finalize( buffer, Type.clearAttribute, length);
    connection.write( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleClearAttribute( Connection connection, ByteBuffer buffer, int length)
  {
    String xpath = readString( buffer);
    String attrName = readString( buffer);
    handleClearAttribute( connection, xpath, attrName);
  }
  
  /**
   * Handle an attribute change message.
   * @param connection The connection.
   * @param xpath The xpath from the root to the element.
   * @param attrName The name of the attribute.
   */
  protected void handleClearAttribute( Connection connection, String xpath, String attrName)
  {
  }

  /**
   * Send a query request message.
   * @param connection The connection.
   * @param xpath The xpath.
   */
  public final void sendQueryRequest( Connection connection, String xpath) throws IOException
  {
    initialize( buffer);
    byte[] bytes = xpath.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.queryRequest, bytes.length);
    send( connection, buffer, timeout);
  }
  
  /**
   * Handle the specified message buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleQueryRequest( Connection connection, ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    handleQueryRequest( connection, new String( bytes));
  }
  
  /**
   * Handle a query requset.
   * @param connection The connection.
   * @param xpath The query.
   */
  protected void handleQueryRequest( Connection connection, String xpath)
  {
  }
  
  /**
   * Send a query response message.
   * @param connection The connection.
   * @param result The result.
   */
  public final void sendQueryResponse( Connection connection, Object result) throws IOException
  {
    initialize( buffer);
    byte[] bytes = serialize( result);
    buffer.put( bytes);
    finalize( buffer, Type.queryResponse, bytes.length);

    // wait for response
    ByteBuffer response = send( connection, buffer, timeout);
    handleQueryResponse( connection, response.array());
  }
  
  /**
   * Handle the specified message buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleQueryResponse( Connection connection, ByteBuffer buffer, int length)
  {
    ByteBuffer response = ByteBuffer.allocate( length);
    buffer.get( response.array(), 0, length);
    response.limit( length);
    response.flip();
    try { responseQueue.put( response);} catch( InterruptedException e) {}
  }
  
  /**
   * Handle a query requset.
   * @param connection The connection.
   * @param bytes The serialized query result.
   */
  protected void handleQueryResponse( Connection connection, byte[] bytes)
  {
  }
  
  /**
   * Send a debug step message.
   * @param connection The connection.
   */
  public final void sendDebugStepIn( Connection connection) throws IOException
  {
    initialize( buffer);
    finalize( buffer, Type.debugStepIn, 0);
    connection.write( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleDebugStepIn( Connection connection, ByteBuffer buffer, int length)
  {
    handleDebugStepIn( connection);
  }
  
  /**
   * Handle a debug step.
   * @param connection The connection.
   */
  protected void handleDebugStepIn( Connection connection)
  {
  }
  
  /**
   * Send a debug step message.
   * @param connection The connection.
   */
  public final void sendDebugStepOver( Connection connection) throws IOException
  {
    initialize( buffer);
    finalize( buffer, Type.debugStepOver, 0);
    connection.write( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleDebugStepOver( Connection connection, ByteBuffer buffer, int length)
  {
    handleDebugStepOver( connection);
  }
  
  /**
   * Handle a debug step.
   * @param connection The connection.
   */
  protected void handleDebugStepOver( Connection connection)
  {
  }
  
  /**
   * Send a debug step message.
   * @param connection The connection.
   */
  public final void sendDebugStepOut( Connection connection) throws IOException
  {
    initialize( buffer);
    finalize( buffer, Type.debugStepOut, 0);
    connection.write( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param connection The connection.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleDebugStepOut( Connection connection, ByteBuffer buffer, int length)
  {
    handleDebugStepOut( connection);
  }
  
  /**
   * Handle a debug step.
   * @param connection The connection.
   */
  protected void handleDebugStepOut( Connection connection)
  {
  }

  /**
   * Send and wait for a response.
   * @param connection The connection.
   * @param buffer The buffer to send.
   * @param timeout The timeout in milliseconds.
   * @return Returns null or the response buffer.
   */
  private ByteBuffer send( Connection connection, ByteBuffer buffer, int timeout) throws IOException
  {
    try
    {
      connection.write( buffer);
      ByteBuffer response = responseQueue.poll( timeout, TimeUnit.MILLISECONDS);
      if ( response != null) return response;
    }
    catch( InterruptedException e)
    {
      return null;
    }
    
    throw new IOException( "Network request timeout.");
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

  private static Log log = Log.getLog(  "org.xmodel.net");

  protected ICompressor compressor;
  private ByteBuffer buffer;
  private BlockingQueue<ByteBuffer> responseQueue;
  private int timeout;
}
