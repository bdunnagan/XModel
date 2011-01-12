package org.xmodel.xaction;

import java.io.File;
import java.io.FileInputStream;

import org.xmodel.IModelObject;
import org.xmodel.xml.XmlIO;

public class Main
{
  public static void main( String[] args) throws Exception
  {
    XmlIO xmlIO = new XmlIO();
    IModelObject root = xmlIO.read( new FileInputStream( new File( args[ 0])));
    XActionDocument doc = new XActionDocument( root);
    IXAction script = doc.createScript( root);
    script.run();
  }
}
