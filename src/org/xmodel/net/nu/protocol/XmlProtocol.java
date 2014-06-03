package org.xmodel.net.nu.protocol;

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
    threadXmlIO = new ThreadLocal<XmlIO>();
  }
  
  @Override
  public byte[] encode( IModelObject message)
  {
    String xml = getThreadData().write( message);
    return xml.getBytes( charset);
  }

  @Override
  public IModelObject decode( byte[] message, int offset, int length) throws XmlException
  {
    String xml = new String( message, offset, length, charset);
    return getThreadData().read( xml);
  }
  
  private XmlIO getThreadData()
  {
    XmlIO xmlIO = threadXmlIO.get();
    if ( xmlIO == null)
    {
      xmlIO = new XmlIO();
      threadXmlIO.set( xmlIO);
    }
    return xmlIO;
  }
  
  private Charset charset;
  private ThreadLocal<XmlIO> threadXmlIO;
}
