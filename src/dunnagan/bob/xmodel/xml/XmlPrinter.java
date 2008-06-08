/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IModelPrinter;

/**
 * An implementation of IModelPrinter which renders XML.
 * @deprecated
 */
public class XmlPrinter implements IModelPrinter
{
  public XmlPrinter()
  {
    converter = new XmlConverter();
  }
  
  /**
   * Convenience method for printing a tree to stdout.
   * @param object The root of the tree.
   */
  public static void printTree( IModelObject object)
  {
    XmlPrinter printer = new XmlPrinter();
    System.out.println( printer.renderTree( object));
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelPrinter#printTree(dunnagan.bob.xmodel.IModelObject)
   */
  public String renderTree( IModelObject object)
  {
    Document document = converter.convert( object);
    XMLOutputter printer = new XMLOutputter( Format.getPrettyFormat());
    return printer.outputString( document);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelPrinter#printObject(dunnagan.bob.xmodel.IModelObject)
   */
  public String renderObject( IModelObject object)
  {
    IXmlConversion conversion = converter.getConversion();
    Element element = conversion.transform( object, null);
    XMLOutputter printer = new XMLOutputter( Format.getPrettyFormat());
    return printer.outputString( element);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelPrinter#printTree(java.io.OutputStream, dunnagan.bob.xmodel.IModelObject)
   */
  public void printTree( OutputStream stream, IModelObject object) throws IOException
  {
    Document document = converter.convert( object);
    XMLOutputter printer = new XMLOutputter( Format.getPrettyFormat());
    printer.output( document, stream);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelPrinter#printObject(java.io.OutputStream, dunnagan.bob.xmodel.IModelObject)
   */
  public void printObject( OutputStream stream, IModelObject object) throws IOException
  {
    String result = renderObject( object);
    PrintStream pstream = new PrintStream( stream);
    pstream.print( result);
  }
  
  IXmlConverter converter;
}
