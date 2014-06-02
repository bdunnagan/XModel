package org.xmodel.net.nu.protocol;

import java.nio.charset.Charset;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.IProtocol;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;

public class XmlProtocol implements IProtocol
{
  public XmlProtocol()
  {
    charset = Charset.forName( "UTF-8");
  }
  
  @Override
  public byte[] encode( IModelObject message)
  {
    String xml = XmlIO.write( Style.printable, message);
    return xml.getBytes( charset);
  }

  @Override
  public IModelObject decode( byte[] message, int offset, int length)
  {
    String xml = new String( message, offset, length, charset);
    return null;
  }
  
  private Charset charset;
}
