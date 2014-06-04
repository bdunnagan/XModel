package org.xmodel.net.nu.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.IWireProtocol;
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
      Header.writeInt( bytes.length-4, bytes, 0);
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
    int messageLength = Header.readInt( message, offset);
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
    
    int messageLength = Header.readInt( reserve, 0);
    if ( messageLength > buffer.remaining()) return null;
    
    try
    {
      return xmlIO.read( new ByteBufferInputStream( buffer));
    }
    catch( XmlException e)
    {
      throw new IOException( e);
    }
  }

  private XmlIO xmlIO;
  private ByteArrayOutputStream stream;
  private byte[] reserve;
}
