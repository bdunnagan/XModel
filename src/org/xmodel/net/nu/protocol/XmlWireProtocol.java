package org.xmodel.net.nu.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.xmodel.IModelObject;
import org.xmodel.util.ByteBufferInputStream;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;

public class XmlWireProtocol implements IWireProtocol
{
  public XmlWireProtocol()
  {
    xmlIO = new XmlIO();
    stream = new ByteArrayOutputStream();
    reserve = new byte[ 4];
  }
  
  @Override
  public byte[] encode( IModelObject message) throws IOException
  {
    stream.reset();
    stream.write( reserve);
    try
    {
      xmlIO.write( message, stream);
      byte[] bytes = stream.toByteArray();
      writeInt( bytes.length-4, bytes, 0);
      return bytes;
    }
    catch( XmlException e)
    {
      throw new IOException( e);
    }
  }

  @Override
  public IModelObject decode( byte[] message, int offset, int length) throws IOException
  {
    if ( length < 4) return null;
    
    length -= 4;
    int messageLength = readInt( message, offset);
    if ( messageLength > length) return null;
    
    try
    {
      return xmlIO.read( new ByteArrayInputStream( message, offset + 4, length));
    }
    catch( XmlException e)
    {
      throw new IOException( e);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.IProtocol#decode(java.nio.ByteBuffer)
   */
  @Override
  public IModelObject decode( ByteBuffer buffer) throws IOException
  {
    if ( buffer.remaining() < 4) return null;
    
    buffer.get( reserve);
    
    int messageLength = readInt( reserve, 0);
    if ( messageLength > buffer.remaining()) return null;
    
    int bufferLimit = buffer.limit();
    buffer.limit( buffer.position() + messageLength);
    
    try
    {
      return xmlIO.read( new ByteBufferInputStream( buffer));
    }
    catch( XmlException e)
    {
      throw new IOException( e);
    }
    finally
    {
      buffer.limit( bufferLimit);
    }
  }
  
  private static int readInt( byte[] bytes, int offset)
  {
    int i;
    i = (int)bytes[ offset++] & 0xFF;
    i |= ((int)bytes[ offset++] & 0xFF) << 8;
    i |= ((int)bytes[ offset++] & 0xFF) << 16;
    i |= ((int)bytes[ offset++] & 0xFF) << 24;
    return i;
  }
  
  private static void writeInt( int i, byte[] bytes, int offset)
  {
    bytes[ offset++] = (byte)(i & 0xFF); i >>= 8;
    bytes[ offset++] = (byte)(i & 0xFF); i >>= 8;
    bytes[ offset++] = (byte)(i & 0xFF); i >>= 8;
    bytes[ offset++] = (byte)(i & 0xFF);
  }

  private XmlIO xmlIO;
  private ByteArrayOutputStream stream;
  private byte[] reserve;
}
