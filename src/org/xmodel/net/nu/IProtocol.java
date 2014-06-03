package org.xmodel.net.nu;

import org.xmodel.IModelObject;
import org.xmodel.xml.XmlException;

public interface IProtocol
{
  public byte[] encode( IModelObject message);
  
  public IModelObject decode( byte[] message, int offset, int length) throws XmlException;
}
