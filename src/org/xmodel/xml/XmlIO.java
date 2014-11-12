/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * XmlIO.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelObjectFactory;
import org.xmodel.Xlate;
import org.xmodel.log.Log;
import org.xmodel.xpath.AttributeNode;
import org.xmodel.xpath.TextNode;

/**
 * An implementation of IXmlIO intended for creating data-models without necessarily
 * incurring the overhead associated with comments, tracking the position of elements
 * embedded in parent text or schema validation. 
 */
public class XmlIO implements IXmlIO
{
  public XmlIO()
  {
    factory = new ModelObjectFactory();
    skipInputPrefixList = new ArrayList<String>( 3);
    skipOutputPrefixList = new ArrayList<String>( 3);
    lines = new ArrayList<IModelObject>();
    maxLines = 0;
    whitespace = Whitespace.trim;
    oneLineElements = true;
    lineNumberTracking = true;
    cycleBreaking = false;
    
    try
    {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(false);
      factory.setFeature("http://xml.org/sax/features/namespaces", false);
      factory.setFeature("http://xml.org/sax/features/validation", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);      
      parser = factory.newSAXParser();
    }
    catch( SAXException e)
    {
      log.exception( e);
    }
    catch( ParserConfigurationException e)
    {
      log.exception( e);
    }
  }
  
  /**
   * Write the specified element using the specified style.
   * @param style The style.
   * @param element The element.
   * @return Returns the xml.
   */
  public static String write( Style style, IModelObject element)
  {
    XmlIO xmlIO = new XmlIO();
    xmlIO.setOutputStyle( style);
    return xmlIO.write( element);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#setWhitespace(org.xmodel.xml.IXmlIO.Whitespace)
   */
  public void setWhitespace( Whitespace whitespace)
  {
    this.whitespace = whitespace;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#setFactory(org.xmodel.IModelObjectFactory)
   */
  public void setFactory( IModelObjectFactory factory)
  {
    this.factory = factory;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#setMaxLines(int)
   */
  public void setMaxLines( int count)
  {
    maxLines = count;
  }
  
  public void setIncludeHeader( boolean includeHeader)
  {
    this.outputHeader = includeHeader;
  }

  /**
   * Set whether a document node is created.
   * @param useDocument True if a document node should be created.
   */
  public void setUseDocument( boolean useDocument)
  {
    this.useDocument = useDocument;
  }
  
  /**
   * Specify whether empty elements are written as compact start+end tags. Default: true.
   * @param flag True if start+end tags should be used.
   */
  public void setOneLineElements( boolean flag)
  {
    oneLineElements = flag;
  }
  
  /**
   * Specify whether the text position of child elements should be recorded. Setting this
   * option will also cause whitespace to be preserved. Default: false.
   * @param flag True if text position should be recorded.
   */
  public void setRecordElementPosition( boolean flag)
  {
    recordTextPosition = flag;
    whitespace = Whitespace.keep;
  }
  
  /**
   * Specify whether line number tracking is enabled. When enabled, the getLineInformation
   * method will return a list containing elements by line number. Default: true.
   * @param flag True 
   */
  public void setLineNumberTracking( boolean flag)
  {
    lineNumberTracking = flag;
  }
  
  /**
   * Specify whether cycle-breaking should be used when writing. Cycles can occur in 
   * data-models that use References.  Default: false.
   * @param flag True if cycle-breaking should be used.
   */
  public void setCycleBreaking( boolean flag)
  {
    cycleBreaking = flag;
  }
  
  /**
   * Set the sax parser error handler.
   * @param handler The error handler.
   */
  public void setErrorHandler( ErrorHandler handler)
  {
    errorHandler = handler;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#read(java.lang.String)
   */
  public IModelObject read( String xml) throws XmlException
  {
    return read( new StringReader( xml));
  }

  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#read(java.net.URL)
   */
  public IModelObject read( URL url) throws XmlException
  {
    try
    {
      InputStream stream = url.openStream();
      read( stream);
      stream.close();
      return root;
    }
    catch( IOException e)
    {
      throw new XmlException( "Unable to parse document from stream.", e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#read(java.io.InputStream)
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

  /**
   * Parse the document with a Reader.
   * @return Returns the parsed document root.
   */
  protected IModelObject read( Reader reader) throws XmlException
  {
    try
    {
      root = null;
      InputSource source = new InputSource( reader);
      parser.parse( source, contentListener);
      return root;
    }
    catch( Exception e)
    {
      throw new XmlException( "Unable to parse document from stream.", e);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#getLineInformation()
   */
  public List<IModelObject> getLineInformation()
  {
    return lines;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#write(org.xmodel.IModelObject)
   */
  public String write( IModelObject root)
  {
    return write( 0, root);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#write(org.xmodel.IModelObject, java.io.File)
   */
  public void write( IModelObject root, File file) throws XmlException
  {
    write( 0, root, file);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#write(org.xmodel.IModelObject, java.io.OutputStream)
   */
  public void write( IModelObject root, OutputStream stream) throws XmlException
  {
    write( 0, root, stream);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#write(int, org.xmodel.IModelObject, java.io.OutputStream)
   */
  public void write( int depth, IModelObject root, OutputStream stream) throws XmlException
  {
    try
    {
      line = 0; lines.clear();
      if ( outputHeader) { stream.write( header); stream.write( cr);}
      output( depth, root, stream);
    }
    catch( IOException e)
    {
      throw new XmlException( "Unable to write xml to stream: "+root, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#write(int, org.xmodel.IModelObject, java.io.File)
   */
  public void write( int depth, IModelObject root, File file) throws XmlException
  {
    try
    {
      line = 0; lines.clear();
      FileOutputStream stream = new FileOutputStream( file);
      if ( outputHeader) { stream.write( header); stream.write( cr);}
      output( depth, root, stream);
    }
    catch( IOException e)
    {
      throw new XmlException( "Unable to write xml to file: "+file+", "+root, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#write(int, org.xmodel.IModelObject)
   */
  public String write( int depth, IModelObject root)
  {
    if ( buffer == null) buffer = new ByteArrayOutputStream( 1<<15);
    buffer.reset();
    try
    {
      line = 0; lines.clear();
      write( depth, root, buffer);
      return buffer.toString(); 
    }
    catch( XmlException e)
    {
      log.exception( e);
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
    // observe line-count limit
    if ( maxLines > 0 && lines.size() > maxLines)
    {
      stream.write( ellipsis);
      return;
    }
    
    // annotate root if requested
    for ( int i = lines.size(); i < line+1; i++) lines.add( null);
    lines.set( line, root);
    
    // check for AttributeNode or TextNode
    if ( root instanceof AttributeNode)
    {
      if ( style != Style.compact) for ( int i=0; i<indent; i++) stream.write( space);
      String attribute = root.getType();
      if ( attribute.charAt( 0) != '!')
      {
        stream.write( at); stream.write( attribute.getBytes( charset)); stream.write( equals);
        Object value = root.getValue();
        stream.write( doubleQuote);
        if ( value != null) stream.write( value.toString().getBytes( charset)); 
        stream.write( doubleQuote);
      }
      return;
    }
    
    if ( root instanceof TextNode)
    {
      output( indent, root.getParent(), stream);
      return;
    }
    
    // check for processing-instruction
    if ( root.getType().length() > 0 && root.getType().charAt( 0) == '?')
    {
      if ( style != Style.compact) for ( int i=0; i<indent; i++) stream.write( space);
      stream.write( less);
      stream.write( root.getType().getBytes( charset));
      String value = Xlate.get( root, "");
      if ( value.length() > 0)
      {
        stream.write( space);
        writeAttributes( root, stream);
        stream.write( space);
        stream.write( value.getBytes( charset));
      }
      else
      {
        writeAttributes( root, stream);
      }
      stream.write( qmark);
      stream.write( greater);
      if ( style != Style.compact) writeCR( stream);
      return;
    }

    // perform cycle-breaking
    if ( cycleBreaking)
    {
      if ( cycleSet == null) cycleSet = new HashSet<IModelObject>();
      
      // check cycle set
      if ( cycleSet.contains( root))
      {
        // reference tag
        if ( style != Style.compact) for ( int i=0; i<indent; i++) stream.write( space);
        stream.write( less);
        String type = root.getType();
        stream.write( type.getBytes( charset));
        writeAttributes( root, stream);
        stream.write( greater);
  
        // ellipsis content
        stream.write( unexpanded);
        
        // end tag
        stream.write( less);
        stream.write( slash);
        stream.write( type.getBytes( charset));
        stream.write( greater);
        if ( style != Style.compact) writeCR( stream);
        return;
      }
      
      // add to cycle set if reference
      if ( root.getReferent() != root) cycleSet.add( root);
    }
    
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
        stream.write( type.getBytes( charset));
        writeAttributes( root, stream);
        stream.write( greater);
        
        // 
        // Whitespace and element text position are linked. If whitespace is trimmed but
        // the output style is printable, then the value of the element will be written
        // first folowed by a carriage return and then the children.
        //
        if ( whitespace == Whitespace.trim)
        {
          // text
          if ( value != null) stream.write( encodeEntityReferences( value, "\"\'").getBytes( charset));
          if ( style != Style.compact) writeCR( stream);
          
          // children
          for( IModelObject child: children) output( indent+2, child, stream);
        }
        else
        {
          // text and children interspersed
          if ( value != null)
          {
            value = encodeEntityReferences( value, "\"\'");        
            int start = 0;
            for( IModelObject child: children) 
            {
              int index = Xlate.get( child, "!position", -1);
              if ( index < 0) index = value.length();
              stream.write( value.substring( start, index).getBytes( charset));
              start = index;
              output( indent+2, child, stream);
            }
            stream.write( value.substring( start).getBytes( charset));
          }
          else
          {
            // children
            for( IModelObject child: children) 
              output( indent+2, child, stream);
          }
        }
                
        // end tag
        if ( style != Style.compact) for ( int i=0; i<indent; i++) stream.write( space);
        stream.write( less);
        stream.write( slash);
        stream.write( type.getBytes( charset));
        stream.write( greater);
        if ( style != Style.compact) writeCR( stream);
      }
      // safari and firefox html parsers do not like start+end tag
      else if ( (value != null && value.length() > 0) || !oneLineElements)
      {
        // start tag
        if ( style != Style.compact) for ( int i=0; i<indent; i++) stream.write( space);
        stream.write( less);
        stream.write( type.getBytes( charset));
        writeAttributes( root, stream);
        stream.write( greater);
        
        // value
        if ( value != null) stream.write( encodeEntityReferences( value, "\"\'").getBytes( charset));
        
        // end tag
        if ( value != null && value.length() > 0 && value.charAt( value.length() - 1) == '\n' && style != Style.compact) 
          for ( int i=0; i<indent; i++) stream.write( space);
        stream.write( less);
        stream.write( slash);
        stream.write( type.getBytes( charset));
        stream.write( greater);
        if ( style != Style.compact) writeCR( stream);
      }
      else
      {
        // start tag
        if ( style != Style.compact) for ( int i=0; i<indent; i++) stream.write( space);
        stream.write( less);
        stream.write( type.getBytes( charset));
        writeAttributes( root, stream);
        stream.write( slash);
        stream.write( greater);
        if ( style != Style.compact) writeCR( stream);
      }
    }
    finally
    {
      // remove from cycle set if reference
      if ( cycleSet != null && root.getReferent() != root) cycleSet.remove( root);
    }
  }
  
  /**
   * Write the specified value surrounded by either single or double quotes, as necessary.
   * @param stream The stream.
   * @param value The value.
   */
  private void writeInQuotes( OutputStream stream, String value) throws IOException
  {
    int index = 0;
    for( ; index < value.length(); index++)
    {
      char c = value.charAt( index);
      if ( c == '\'')
      {
        break;
      }
      else if ( c == '\"')
      {
        stream.write( singleQuote);
        stream.write( encodeEntityReferences( value, "\'\"").getBytes( charset));
        stream.write( singleQuote);
        return;
      }
    }
    
    stream.write( doubleQuote);
    stream.write( encodeEntityReferences( value, "\'\"").getBytes( charset));
    stream.write( doubleQuote);
  }    
  
  /**
   * Write a carriage return to the output stream and return the line number of the previous line.
   * @param stream The output stream.
   * @return Returns the current line number.
   */
  protected int writeCR( OutputStream stream) throws IOException
  {
    stream.write( cr);
    return line++;
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
      if ( attrName.length() == 0 || attrName.charAt( 0) == '!') continue;
      
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
        stream.write( attrName.getBytes( charset));
        stream.write( equals);
        writeInQuotes( stream, attrValue);
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#skipInputPrefix(java.lang.String)
   */
  public void skipInputPrefix( String prefix)
  {
    skipInputPrefixList.add( prefix);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#skipOutputPrefix(java.lang.String)
   */
  public void skipOutputPrefix( String prefix)
  {
    skipOutputPrefixList.add( prefix);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlIO#setOutputStyle(org.xmodel.xml.IXmlIO.Style)
   */
  public void setOutputStyle( Style style)
  {
    this.style = style;
  }

  /**
   * Encode (special) entity references.
   * @param s A string containing references.
   * @param ignore Characters that should not be encoded.
   * @return A string with references encoded.
   */
  public static String encodeEntityReferences( String s, String ignore)
  {
    if ( s == null) return s;
    int len = s.length();
    StringBuilder sb = new StringBuilder( len);
    for ( int i = 0; i < len; i++)
    {
      char c = s.charAt( i);
      if ( ignore == null || ignore.indexOf( c) == -1)
      {
        switch ( c)
        {
          case '<': sb.append( "&lt;"); break;
          case '>': sb.append( "&gt;"); break;
          case '&': sb.append( "&amp;"); break;
          case '\'': sb.append( "&apos;"); break;
          case '"': sb.append( "&quot;"); break;
          default: sb.append( c);
        }
      }
      else
      {
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
      root = useDocument? factory.createObject( null, "document"): null;
      parent = root;
      lines.clear();
    }
    public void endDocument() throws SAXException
    {
    }
    public void setDocumentLocator( Locator locator)
    {
      XmlIO.this.locator = locator;
    }
    public void startElement( String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
      if ( skipInputPrefixList.size() > 0)
      {
        int index = qName.indexOf( ":");
        String prefix = (index >= 0)? qName.substring( 0, index): "";
        if ( skipInputPrefixList.contains( prefix)) return; 
      }
      
      // create element
      child = factory.createObject( parent, attributes, qName);  
      
      // set id
      String id = attributes.getValue( "id");
      if ( id != null) child.setAttribute( "id", id);
      
      // record text position
      if ( parent != null && recordTextPosition)
      {
        StringBuilder value = (StringBuilder)parent.getValue();
        int position = (value != null)? value.length(): 0;
        child.setAttribute( "!position", position);
      }
      
      // line tracking
      if ( lineNumberTracking)
      {
        int line = locator.getLineNumber()-1;
        for( int i=lines.size(); i<line+1; i++) lines.add( null);
        lines.set( line, child);
      }

      // set attributes
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

      // set document root or add to parent
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
      if ( whitespace == Whitespace.trim)
      {
        String value = Xlate.get( parent, "");
        value = value.trim();
        if ( value.length() > 0) parent.setValue( value); else parent.removeAttribute( "");
      }
      
      // clear child
      child = null;
      
      // update parent
      if ( parent.getParent() == null) root = parent;
      parent = parent.getParent();
    }
    public void processingInstruction( String target, String data) throws SAXException
    {
      if ( parent != null)
      {
        IModelObject pi = factory.createObject( parent, "?"+target);
        
        // line tracking
        int line = locator.getLineNumber()-1;
        for( int i=lines.size(); i<line+1; i++) lines.add( null);
        lines.set( line, pi);
        
        // set pi data
        pi.setValue( data);
        parent.addChild( pi);
      }
    }
    public void warning( SAXParseException e) throws SAXException
    {
      if ( errorHandler != null) errorHandler.warning( e);
      super.warning( e);
    }
    public void error( SAXParseException e) throws SAXException
    {
      if ( errorHandler != null) errorHandler.error( e);
      super.error( e);
    }
    public void fatalError( SAXParseException e) throws SAXException
    {
      if ( errorHandler != null) errorHandler.fatalError( e);
      super.fatalError( e);
    }
  };
  
  private final static Charset charset = Charset.forName( "UTF-8");
  public final static byte[] header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes( charset);
  public final static byte[] space = " ".getBytes( charset);
  public final static byte[] less = "<".getBytes( charset);
  public final static byte[] greater = ">".getBytes( charset);
  public final static byte[] singleQuote = "\'".getBytes( charset);
  public final static byte[] doubleQuote = "\"".getBytes( charset);
  public final static byte[] equals = "=".getBytes( charset);
  public final static byte[] slash = "/".getBytes( charset);
  public final static byte[] text= "text()".getBytes( charset);
  public final static byte[] at = "@".getBytes( charset);
  public final static byte[] qmark = "?".getBytes( charset);
  public final static byte[] cr = "\n".getBytes( charset);
  public final static byte[] ellipsis = "...".getBytes( charset);
  public final static byte[] unexpanded = "(unexpanded reference)".getBytes( charset);
  
  private SAXParser parser;
  private Whitespace whitespace;
  private ErrorHandler errorHandler;
  private IModelObjectFactory factory;
  private IModelObject root;
  private IModelObject parent;
  private IModelObject child;
  private ByteArrayOutputStream buffer;
  private List<String> skipInputPrefixList;
  private List<String> skipOutputPrefixList;
  private Style style;
  private Set<IModelObject> cycleSet;
  private int maxLines;
  private int line;
  private List<IModelObject> lines;
  private Locator locator;
  private boolean useDocument;
  private boolean outputHeader;
  private boolean oneLineElements;
  private boolean recordTextPosition;
  private boolean lineNumberTracking;
  private boolean cycleBreaking;
  
  private static Log log = Log.getLog( XmlIO.class);
  
  public static void main( String[] args) throws Exception
  {
    String text = 
        "<?xml-stylesheet href=\"nmap.xsl\" type=\"text/xsl\"?>\n" + 
        "<root/>";
    
    XmlIO io = new XmlIO();
    io.setUseDocument( true);
    IModelObject root = io.read( new StringReader( text));
    System.out.println( io.write( root));
  }
}
