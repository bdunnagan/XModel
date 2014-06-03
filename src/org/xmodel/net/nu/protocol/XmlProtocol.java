package org.xmodel.net.nu.protocol;

import java.io.IOException;
import java.nio.charset.Charset;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.IProtocol;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;

public class XmlProtocol implements IProtocol
{
  public XmlProtocol()
  {
    charset = Charset.forName( "UTF-8");
    xmlIO = new XmlIO();
  }
  
  @Override
  public byte[] encode( IModelObject message)
  {
    String xml = xmlIO.write( message);
    return xml.getBytes( charset);
  }

  @Override
  public IModelObject decode( byte[] message, int offset, int length) throws IOException
  {
    try
    {
      String xml = new String( message, offset, length, charset);
      return xmlIO.read( xml);
    }
    catch( XmlException e)
    {
      throw new IOException( e);
    }
  }
  
  private Charset charset;
  private XmlIO xmlIO;
}
