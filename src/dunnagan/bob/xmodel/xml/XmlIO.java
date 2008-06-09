/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xml;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dunnagan.bob.xmodel.*;
import dunnagan.bob.xmodel.xpath.AttributeNode;
import dunnagan.bob.xmodel.xpath.TextNode;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

public class XmlIO implements IXmlIO
{
  public XmlIO()
  {
    factory = new ModelObjectFactory();
    skipInputPrefixList = new ArrayList<String>( 3);
    skipOutputPrefixList = new ArrayList<String>( 3);
    cycleSet = new HashSet<IModelObject>();
    
    try
    {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      parser = factory.newSAXParser();
    }
    catch( SAXException e)
    {
      e.printStackTrace( System.err);
    }
    catch( ParserConfigurationException e)
    {
      e.printStackTrace( System.err);
    }
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlIO#setFactory(dunnagan.bob.xmodel.IModelObjectFactory)
   */
  public void setFactory( IModelObjectFactory factory)
  {
    this.factory = factory;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlIO#read(java.lang.String)
   */
  public IModelObject read( String xml) throws XmlException
  {
    root = null;
    ByteArrayInputStream stream = new ByteArrayInputStream( xml.getBytes());
    return read( stream);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlIO#read(java.net.URL)
   */
  public IModelObject read( URL url) throws XmlException
  {
    try
    {
      root = null;
      parser.parse( url.toURI().toString(), contentListener);
      return root;
    }
    catch( Exception e)
    {
      throw new XmlException( "Unable to parse document: "+url, e);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlIO#read(java.io.InputStream)
   */
  public IModelObject read( InputStream stream) throws XmlException
  {
    try
    {
      root = null;
      parser.parse( stream, contentListener);
      return root;
    }
    catch( Exception e)
    {
      throw new XmlException( "Unable to parse document from stream.", e);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlIO#write(dunnagan.bob.xmodel.IModelObject)
   */
  public String write( IModelObject root)
  {
    return write( 0, root);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlIO#write(dunnagan.bob.xmodel.IModelObject, java.io.File)
   */
  public void write( IModelObject root, File file) throws XmlException
  {
    write( 0, root, file);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlIO#write(dunnagan.bob.xmodel.IModelObject, java.io.OutputStream)
   */
  public void write( IModelObject root, OutputStream stream) throws XmlException
  {
    write( 0, root, stream);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlIO#write(int, dunnagan.bob.xmodel.IModelObject, java.io.OutputStream)
   */
  public void write( int depth, IModelObject root, OutputStream stream) throws XmlException
  {
    try
    {
      output( depth, root, stream);
    }
    catch( IOException e)
    {
      throw new XmlException( "Unable to write xml to stream: "+root, e);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlIO#write(int, dunnagan.bob.xmodel.IModelObject, java.io.File)
   */
  public void write( int depth, IModelObject root, File file) throws XmlException
  {
    try
    {
      FileOutputStream stream = new FileOutputStream( file);
      output( depth, root, stream);
    }
    catch( IOException e)
    {
      throw new XmlException( "Unable to write xml to file: "+file+", "+root, e);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlIO#write(int, dunnagan.bob.xmodel.IModelObject)
   */
  public String write( int depth, IModelObject root)
  {
    if ( buffer == null) buffer = new ByteArrayOutputStream( 1<<15);
    buffer.reset();
    try
    {
      write( depth, root, buffer);
      return buffer.toString(); 
    }
    catch( XmlException e)
    {
      e.printStackTrace( System.err);
      return null;
    }
  }

  /**
   * Write the specified subtree to the specified stream with indentation.
   * @param indent The number of spaces to indent.
   * @param root The root of the subtree.
   * @param stream The stream.
   */
  protected void output( int indent, IModelObject root, OutputStream stream) throws IOException
  {
    // check for AttributeNode or TextNode
    if ( root instanceof AttributeNode)
    {
      if ( style != Style.compact) for ( int i=0; i<indent; i++) stream.write( space);
      String attribute = root.getType();
      stream.write( at); stream.write( attribute.getBytes()); stream.write( equals);
      Object value = root.getValue();
      stream.write( quote);
      if ( value != null) stream.write( value.toString().getBytes()); 
      stream.write( quote);
      return;
    }
    
    if ( root instanceof TextNode)
    {
      output( indent, root.getParent(), stream);
      return;
    }
    
    // check for processing-instruction
    if ( root.getType().charAt( 0) == '?')
    {
      if ( style != Style.compact) for ( int i=0; i<indent; i++) stream.write( space);
      stream.write( less);
      stream.write( root.getType().getBytes());
      String value = Xlate.get( root, "");
      if ( value.length() > 0)
      {
        stream.write( space);
        writeAttributes( root, stream);
        stream.write( space);
        stream.write( value.getBytes());
      }
      else
      {
        writeAttributes( root, stream);
      }
      stream.write( qmark);
      stream.write( greater);
      if ( style != Style.compact) stream.write( cr);
      return;
    }
    
    // check cycle set
    if ( cycleSet.contains( root))
    {
      // reference tag
      if ( style != Style.compact) for ( int i=0; i<indent; i++) stream.write( space);
      stream.write( less);
      String type = root.getType();
      stream.write( type.getBytes());
      writeAttributes( root, stream);
      stream.write( greater);

      // ellipsis content
      stream.write( unexpanded);
      
      // end tag
      stream.write( less);
      stream.write( slash);
      stream.write( type.getBytes());
      stream.write( greater);
      if ( style != Style.compact) stream.write( cr);
      return;
    }
    
    // add to cycle set if reference
    if ( root.getReferent() != root) cycleSet.add( root);
    
    try
    {
      // write xml
      String type = root.getType();
      if ( skipOutputPrefixList.size() > 0)
      {
        int index = type.indexOf( ":");
        String prefix = (index >= 0)? type.substring( 0, index): "";
        if ( skipOutputPrefixList.contains( prefix)) return;
      }
  
      String value = Xlate.get( root, (String)null);
      List<IModelObject> children = root.getChildren();
      if ( children.size() > 0)
      {
        // start tag
        if ( style != Style.compact) for ( int i=0; i<indent; i++) stream.write( space);
        stream.write( less);
        stream.write( type.getBytes());
        writeAttributes( root, stream);
        stream.write( greater);
        
        // value
        if ( value != null) stream.write( encodeEntityReferences( value, true).getBytes());
        if ( style != Style.compact) stream.write( cr);
        
        // children
        for( IModelObject child: children) output( indent+2, child, stream);
        
        // end tag
        if ( style != Style.compact) for ( int i=0; i<indent; i++) stream.write( space);
        stream.write( less);
        stream.write( slash);
        stream.write( type.getBytes());
        stream.write( greater);
        if ( style != Style.compact) stream.write( cr);
      }
      else if ( value != null && value.length() > 0)
      {
        // start tag
        if ( style != Style.compact) for ( int i=0; i<indent; i++) stream.write( space);
        stream.write( less);
        stream.write( type.getBytes());
        writeAttributes( root, stream);
        stream.write( greater);
        
        // value
        if ( value != null) stream.write( encodeEntityReferences( value, false).getBytes());
        
        // end tag
        if ( value != null && value.charAt( value.length() - 1) == '\n' && style != Style.compact) 
          for ( int i=0; i<indent; i++) stream.write( space);
        stream.write( less);
        stream.write( slash);
        stream.write( type.getBytes());
        stream.write( greater);
        if ( style != Style.compact) stream.write( cr);
      }
      else
      {
        // start tag
        if ( style != Style.compact) for ( int i=0; i<indent; i++) stream.write( space);
        stream.write( less);
        stream.write( type.getBytes());
        writeAttributes( root, stream);
        stream.write( slash);
        stream.write( greater);
        if ( style != Style.compact) stream.write( cr);
      }
    }
    finally
    {
      // remove from cycle set if reference
      if ( root.getReferent() != root) cycleSet.remove( root);
    }
  }
  
  /**
   * Write the attributes of the specified root to the specified stream.
   * @param root The root of the subtree.
   * @param stream The stream.
   */
  protected void writeAttributes( IModelObject root, OutputStream stream) throws IOException
  {
    for ( String attrName: root.getAttributeNames())
    {
      if ( attrName.length() == 0) continue;
      
      if ( skipOutputPrefixList.size() > 0)
      {
        int index = attrName.indexOf( ":");
        String prefix = (index >= 0)? attrName.substring( 0, index): "";
        if ( skipOutputPrefixList.contains( prefix)) continue;
      }
      
      String attrValue = Xlate.get( root, attrName, (String)null);
      if ( attrValue != null)
      {
        stream.write( space);
        stream.write( encodeEntityReferences( attrName, false).getBytes());
        stream.write( equals);
        stream.write( quote);
        stream.write( encodeEntityReferences( attrValue, false).getBytes());
        stream.write( quote);
      }
    }
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlIO#skipInputPrefix(java.lang.String)
   */
  public void skipInputPrefix( String prefix)
  {
    skipInputPrefixList.add( prefix);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlIO#skipOutputPrefix(java.lang.String)
   */
  public void skipOutputPrefix( String prefix)
  {
    skipOutputPrefixList.add( prefix);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.IXmlIO#setOutputStyle(dunnagan.bob.xmodel.xml.IXmlIO.Style)
   */
  public void setOutputStyle( Style style)
  {
    this.style = style;
  }

  /**
   * Encode (special) entity references.
   * @param s A string containing references.
   * @param limited Limit the encoding to (&lt;) and (&gt;)
   * @return A string with references encoded.
   */
  public static String encodeEntityReferences( String s, boolean limited)
  {
    if ( s == null) return s;
    int len = s.length();
    StringBuilder sb = new StringBuilder( len);
    for ( int i = 0; i < len; i++)
    {
      char c = s.charAt( i);
      switch ( c)
      {
        case '<':
          sb.append( "&lt;");
        break;
        case '>':
          sb.append( "&gt;");
        break;
        case '&':
          sb.append( limited? "&": "&amp;");
        break;
        case '\'':
          sb.append( limited? "'": "&apos;");
        break;
        case '"':
          sb.append( limited? "\"": "&quot;");
        break;
        default:
          sb.append( c);
      }
    }

    return sb.toString();
  }

  /**
   * Print the xml for the specified tree.
   * @param tree The root of the tree.
   * @return Returns the xml for the specified tree.
   */
  public static String toString( IModelObject tree)
  {
    XmlIO xmlIO = new XmlIO();
    xmlIO.setOutputStyle( Style.printable);
    return xmlIO.write( tree);
  }
  
  DefaultHandler contentListener = new DefaultHandler() {
    public void startDocument() throws SAXException
    {
      root = null;
      parent = null;
    }
    public void endDocument() throws SAXException
    {
    }
    public void startElement( String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
      if ( skipInputPrefixList.size() > 0)
      {
        int index = qName.indexOf( ":");
        String prefix = (index >= 0)? qName.substring( 0, index): "";
        if ( skipInputPrefixList.contains( prefix)) return; 
      }

      child = factory.createObject( parent, attributes, qName);  
      String id = attributes.getValue( "id");
      if ( id != null) child.setID( id);
      
      int count = attributes.getLength();
      for ( int i=0; i<count; i++)
      {
        String attrName = attributes.getQName( i);
        String attrValue = attributes.getValue( i);
        if ( skipInputPrefixList.size() > 0)
        {
          int index = attrName.indexOf( ":");
          String prefix = (index >= 0)? attrName.substring( 0, index): "";
          if ( skipInputPrefixList.contains( prefix)) continue;
        }
        child.setAttribute( attrName, attrValue);
      }

      if ( parent != null) parent.addChild( child);
      parent = child;
    }
    public void characters( char[] ch, int start, int length) throws SAXException
    {
      if ( child != null)
      {
        StringBuilder value = (StringBuilder)child.getValue();
        if ( value == null)
        {
          value = new StringBuilder();
          child.setValue( value);
        }
        value.append( ch, start, length);
      }
    }
    public void endElement( String uri, String localName, String qName) throws SAXException
    {
      // trim text
      String value = Xlate.get( parent, "");
      value = value.trim();
      if ( value.length() > 0) parent.setValue( value); else parent.removeAttribute( "");
      
      // clear child
      child = null;
      
      // update parent
      if ( parent.getParent() == null) root = parent;
      parent = parent.getParent();
    }
    public void processingInstruction( String target, String data) throws SAXException
    {
      //FIXME: XModel documents do not have a document node so there is no where to put
      // processing instructions at the top of the document!
      if ( parent != null)
      {
        IModelObject pi = factory.createObject( parent, "?"+target);
        pi.setValue( data);
        parent.addChild( pi);
      }
    }
  };

  public final static byte[] header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes();
  public final static byte[] space = " ".getBytes();
  public final static byte[] less = "<".getBytes();
  public final static byte[] greater = ">".getBytes();
  public final static byte[] quote = "\"".getBytes();
  public final static byte[] equals = "=".getBytes();
  public final static byte[] slash = "/".getBytes();
  public final static byte[] text= "text()".getBytes();
  public final static byte[] at = "@".getBytes();
  public final static byte[] qmark = "?".getBytes();
  public final static byte[] cr = "\n".getBytes();
  public final static byte[] unexpanded = "(unexpanded reference)".getBytes();
  
  private SAXParser parser;
  private IModelObjectFactory factory;
  private IModelObject root;
  private IModelObject parent;
  private IModelObject child;
  private ByteArrayOutputStream buffer;
  private List<String> skipInputPrefixList;
  private List<String> skipOutputPrefixList;
  private Style style;
  private Set<IModelObject> cycleSet;
  
  public static void main( String[] args) throws Exception
  {
    String xml = 
      "<root>" +
      "  <x id='H938FX9' status='5'/>" +
      "  <?xx Message('50')?>" +
      "  <y>" +
      "    <?xxx?>" +
      "  </y>" +
      "  <z><![CDATA[!@#%^&*()~?<>]]></z>" +
      "</root>";
    
    XmlIO xmlIO = new XmlIO();
    xmlIO.setOutputStyle( Style.printable);
    IModelObject o = xmlIO.read( xml);
    System.out.println( xmlIO.write( o));
    
    System.out.println( "RESULT");
    IExpression e = XPath.createExpression( "//*");
    for( IModelObject r: e.query( o, null))
      System.out.println( r);
    
    
    IModelObject r = new ModelObject( "root");
    r.setValue( "<><><>");
    xml = xmlIO.write( r);
    System.out.println( xml);
    r = xmlIO.read( xml);
    System.out.println( r);
  }
}
