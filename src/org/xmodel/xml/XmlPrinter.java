/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xmodel.IModelObject;
import org.xmodel.IModelPrinter;

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
   * @see org.xmodel.IModelPrinter#printTree(org.xmodel.IModelObject)
   */
  public String renderTree( IModelObject object)
  {
    Document document = converter.convert( object);
    XMLOutputter printer = new XMLOutputter( Format.getPrettyFormat());
    return printer.outputString( document);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelPrinter#printObject(org.xmodel.IModelObject)
   */
  public String renderObject( IModelObject object)
  {
    IXmlConversion conversion = converter.getConversion();
    Element element = conversion.transform( object, null);
    XMLOutputter printer = new XMLOutputter( Format.getPrettyFormat());
    return printer.outputString( element);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelPrinter#printTree(java.io.OutputStream, org.xmodel.IModelObject)
   */
  public void printTree( OutputStream stream, IModelObject object) throws IOException
  {
    Document document = converter.convert( object);
    XMLOutputter printer = new XMLOutputter( Format.getPrettyFormat());
    printer.output( document, stream);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelPrinter#printObject(java.io.OutputStream, org.xmodel.IModelObject)
   */
  public void printObject( OutputStream stream, IModelObject object) throws IOException
  {
    String result = renderObject( object);
    PrintStream pstream = new PrintStream( stream);
    pstream.print( result);
  }
  
  IXmlConverter converter;
}
