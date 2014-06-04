package org.xmodel.net.nu.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.xmodel.IModelObject;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.net.nu.IProtocol;
import org.xmodel.util.ByteBufferInputStream;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;

public class XipProtocol implements IProtocol
{
  public XipProtocol()
  {
    this( new TabularCompressor());
  }
  
  public XipProtocol( ICompressor compressor)
  {
    this.compressor = compressor;
  }
  
  @Override
  public byte[] encode( IModelObject message) throws IOException
  {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    compressor.compress( message, stream);
    return stream.toByteArray();
  }

  @Override
  public IModelObject decode( byte[] message, int offset, int length) throws IOException
  {
    return compressor.decompress( new ByteArrayInputStream( message, offset, length));
  }

  @Override
  public IModelObject decode( ByteBuffer buffer) throws IOException
  {
    return compressor.decompress( new ByteBufferInputStream( buffer));
  }

  private ICompressor compressor;
  
  public static void main( String[] args) throws Exception
  {
    XipProtocol p = new XipProtocol();
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
      System.out.println( XmlIO.write( Style.printable, p.decode( b)));
  }
}
