/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xml;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;


/**
 * An interface for generating an XModel from an XML document and vice versa.
 */
public interface IXmlIO
{
  public enum Style { compact, printable};
  
  /**
   * Set the factory to use to create objects when parsing XML.
   * @param factory The factory.
   */
  public void setFactory( IModelObjectFactory factory);
  
  /**
   * Read the XML document in the specified string.
   * @param xml A string containing a well-formed XML document.
   * @return Returns the root of the model.
   */
  public IModelObject read( String xml) throws XmlException;
  
  /**
   * Read the XML document at the specified URL.
   * @param url The url pointing to an XML document.
   * @return Returns the root of the model.
   */
  public IModelObject read( URL url) throws XmlException;

  /**
   * Read an XML document from the specified stream.
   * @param stream The stream.
   * @return Returns the root of the model.
   */
  public IModelObject read( InputStream stream) throws XmlException;

  /**
   * Returns a list with one entry per line in the output document. Each entry contains the element whose
   * textual representation in the output document occurs on the line number corresponding to the entry
   * index.  Line numbers which do not have start-tags will be null.  Note that the actual line number
   * is the list index plus one.
   * @return Returns a list representing the element on each line of the output document.
   */
  public List<IModelObject> getLineInformation();
  
  /**
   * Write the subtree with the specified root to a string and return it.
   * @param root The root of the subtree.
   */
  public String write( IModelObject root);
  
  /**
   * Write the subtree with the specified root to the specified file.
   * @param root The root of the subtree.
   * @param file The file to be written.
   */
  public void write( IModelObject root, File file) throws XmlException;
  
  /**
   * Write the subtree with the specified root to the specified PrintStream.
   * @param root The root of the subtree.
   * @param stream The output stream.
   */
  public void write( IModelObject root, OutputStream stream) throws XmlException;
  
  /**
   * Write the subtree with the specified root to a string and return it.
   * @param depth The number of spaces to indent.
   * @param root The root of the subtree.
   */
  public String write( int depth, IModelObject root);
  
  /**
   * Write the subtree with the specified root to the specified file.
   * @param depth The number of spaces to indent.
   * @param root The root of the subtree.
   * @param file The file to be written.
   */
  public void write( int depth, IModelObject root, File file) throws XmlException;
  
  /**
   * Write the subtree with the specified root to the specified PrintStream.
   * @param depth The number of spaces to indent.
   * @param root The root of the subtree.
   * @param stream The output stream.
   */
  public void write( int depth, IModelObject root, OutputStream stream) throws XmlException;
  
  /**
   * Tell the implementation not to generate objects or attributes with the specified prefix on read.
   * @param prefix The prefix to be skipped.
   */
  public void skipInputPrefix( String prefix);
  
  /**
   * Tell the implementation not to generate objects or attributes with the specified prefix on write.
   * @param prefix The prefix to be skipped.
   */
  public void skipOutputPrefix( String prefix);
  
  /**
   * Set the output style.
   * @param style The output style.
   */
  public void setOutputStyle( Style style);
}
