package org.xmodel.net.nu.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.xmodel.IModelObject;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.util.ByteBufferInputStream;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;

public class XipWireProtocol implements IWireProtocol
{
  public XipWireProtocol()
  {
    this( new TabularCompressor());
  }
  
  public XipWireProtocol( ICompressor compressor)
  {
    this.compressor = compressor;
  }
  
  @Override
  public byte[] encode( IModelObject message) throws IOException
  {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    
    stream.write( 0);
    stream.write( 0);
    stream.write( 0);
    stream.write( 0);
    
    compressor.compress( message, stream);
    
    byte[] bytes = stream.toByteArray();
    int ml = bytes.length - 4;
    bytes[ 3] = (byte)(ml & 0xFF);
    bytes[ 2] = (byte)((ml >> 8) & 0xFF);
    bytes[ 1] = (byte)((ml >> 16) & 0xFF);
    bytes[ 0] = (byte)((ml >> 24) & 0xFF);
    
    return bytes;
  }

  @Override
  public IModelObject decode( byte[] message, int offset, int length) throws IOException
  {
    if ( (offset + length) < 4) return null;
    
    int ml = ((int)message[ offset++]) & 0xFF;
    ml <<= 8; ml += ((int)message[ offset++]) & 0xFF;
    ml <<= 8; ml += ((int)message[ offset++]) & 0xFF;
    ml <<= 8; ml += ((int)message[ offset++]) & 0xFF;
    
    if ( (offset + ml) <= length)
    {
      return compressor.decompress( new ByteArrayInputStream( message, offset, length));
    }
    else
    {
      return null;
    }
  }

  @Override
  public IModelObject decode( ByteBuffer buffer) throws IOException
  {
    int ml = buffer.getInt();
    if ( buffer.remaining() >= ml)
    {
      return compressor.decompress( new ByteBufferInputStream( buffer));
    }
    else
    {
      return null;
    }
  }

  private ICompressor compressor;
  
  public static void main( String[] args) throws Exception
  {
    XipWireProtocol p = new XipWireProtocol();
    ByteBuffer b = ByteBuffer.allocate( 1024);
    
    for( int i=0; i<3; i++)
    {
      b.put( p.encode( new XmlIO().read(
        "<message>"+
        "  <print>'Hi'</print>"+
        "</message>"
      )));
    }
    
    b.flip();
    for( int i=0; i<3; i++)
      System.out.printf( "%d: %s", i, XmlIO.write( Style.printable, p.decode( b)));
  }
}
