/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xml;

import java.io.*;
import java.net.URL;
import java.util.List;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import dunnagan.bob.xmodel.*;

/**
 * This class creates a model from an XML document.
 * @deprecated Use XmlIO instead.
 */
public class XmlModel
{
  /**
   * Create an XML model which does not yet have a root.  Use one of the load methods
   * to populate the content of the model.
   */
  public XmlModel()
  {
    converter = new XmlConverter();
  }
  
  /**
   * Create an XML model which parses the XML contained in the specified string.
   * @param xml A string containing XML.
   */
  public XmlModel( String xml) throws IOException
  {
    this();
    this.xml = xml;
    load( xml);
  }
  
  /**
   * Create an XML model from the specified XML file.
   * @param file The file of an XML file.
   */
  public XmlModel( File file) throws IOException
  {
    this();
    this.file = file;
    load( file);
  }
  
  /**
   * Create an XML model from the XML at the specified URL.
   * @param url The URL of an XML resource.
   */
  public XmlModel( URL url) throws IOException
  {
    this();
    this.url = url;
    load( url);
  }

  /**
   * Set the factory to use when creating the model.
   * @param factory The factory to use when creating the model.
   */
  public void setFactory( IModelObjectFactory factory)
  {
    converter = new XmlConverter( factory);
  }
  
  /**
   * Set the IXmlConverter class used to convert between JDOM and XModel representations.
   * @param converter The converter to use when loading.
   */
  public void setConverter( IXmlConverter converter)
  {
    this.converter = converter;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModel#getRoot()
   */
  public IModelObject getRoot()
  {
    return root;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModel#load()
   */
  public void load() throws IOException
  {
    if ( file != null)
      load( file);
    else if ( url != null)
      load( url);
    else if ( xml != null)
      load( xml);
  }
  
  /**
   * Load the model from the specified file.
   * @param file An XML file.
   */
  public void load( File file) throws IOException
  {
    try
    {
      Document document = new SAXBuilder().build( file);
      root = converter.convert( document);
      root.setAttribute( "file", file);
    }
    catch( JDOMException e)
    {
      IOException throwable = new IOException( "JDOM parse error in file: "+file);
      if ( throwable.getCause() == null) throwable.initCause( e);
      throw throwable;
    }
  }
  
  /**
   * Load the model from the specified URL.
   * @param url A URL which points to an XML document.
   */
  public void load( URL url) throws IOException
  {
    try
    {
      Document document = new SAXBuilder().build( url);
      root = converter.convert( document);
      root.setAttribute( "url", url);
    }
    catch( JDOMException e)
    {
      IOException throwable = new IOException( "JDOM parse error from url: "+url);
      if ( throwable.getCause() == null) throwable.initCause( e);
      throw throwable;
    }
  }
  
  /**
   * Load the model from the XML document stored in the specified string.
   * @param xml A string containing an XML document.
   */
  public void load( String xml) throws IOException
  {
    try
    {
      Document document = new SAXBuilder().build( new StringReader( xml));
      root = converter.convert( document);
    }
    catch( JDOMException e)
    {
      IOException throwable = new IOException( "JDOM parse error in xml: "+xml);
      if ( throwable.getCause() == null) throwable.initCause( e);
      throw throwable;
    }
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModel#save()
   */
  public void save() throws IOException
  {
    File file = (File)getRoot().getAttribute( "file");
    FileOutputStream ostream = new FileOutputStream( file);
    XmlPrinter printer = new XmlPrinter();
    printer.printTree( ostream, getRoot());
    ostream.close();
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModel#print(java.io.PrintStream)
   */
  public void print( PrintStream stream) throws IOException
  {
    XmlPrinter printer = new XmlPrinter();
    printer.printTree( stream, getRoot());
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModel#endTransaction()
   */
  public IChangeSet endTransaction()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModel#getTransaction()
   */
  public IChangeSet getTransaction()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModel#startTransaction()
   */
  public IChangeSet startTransaction()
  {
    return null;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModel#endUpdate()
   */
  public void endUpdate()
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModel#restoreUpdate()
   */
  public void restoreUpdate()
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModel#revertUpdate()
   */
  public void revertUpdate()
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModel#startUpdate(dunnagan.bob.xmodel.IModelObject)
   */
  public void startUpdate( IModelObject object)
  {
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModel#setGlobalVariable(java.lang.String, boolean)
   */
  public void setGlobalVariable( String name, boolean value)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModel#setGlobalVariable(java.lang.String, double)
   */
  public void setGlobalVariable( String name, double value)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModel#setGlobalVariable(java.lang.String, java.util.List)
   */
  public void setGlobalVariable( String name, List<IModelObject> nodes)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModel#setGlobalVariable(java.lang.String, java.lang.String)
   */
  public void setGlobalVariable( String name, String value)
  {
  }

  URL url;
  File file;
  String xml;
  IModelObject root;
  IXmlConverter converter;
}
