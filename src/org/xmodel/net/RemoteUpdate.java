package org.xmodel.net;

import java.util.Map;

import org.xmodel.IModelObject;
import org.xmodel.diff.XmlDiffer;
import org.xmodel.external.NonSyncingIterator;
import org.xmodel.xml.IXmlIO;
import org.xmodel.xml.XmlException;

public class RemoteUpdate
{
  public void update( String key, String xml) throws XmlException
  {
    IModelObject lhs = map.get( key);
    IModelObject rhs = xmlIO.read( xml);
    differ.diffAndApply( lhs, rhs);
  }
  
  private Map<String, IModelObject> map;
  private XmlDiffer differ;
  private IXmlIO xmlIO;
}
