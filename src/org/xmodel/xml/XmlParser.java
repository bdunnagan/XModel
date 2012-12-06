package org.xmodel.xml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelObject;
import org.xmodel.ModelObjectFactory;

/**
 * @par An XML 1.0 parser that does not perform validation or any kind of URL lookup.
 * This parser is a work in progress designed to support basic application data
 * modelling requirements. It essentially treats every document like a standalone
 * document.
 *  
 * @par What's supported:
 * @par Full support of XML character class.
 * @par Flexible whitespace handling.
 * @par Internal ENTITY declarations.
 *  
 * @par What's not supported:
 * @par The prolog is treated like any other processing-instruction. 
 * @par DOCTYPE declarations are ignored.
 * @par External ENTITY declarations.
 * @par Language encoding via xml:lang is stored like any attribute.
 *  
 * Specification referenced from: http://www.xml.com/axml/testaxml.htm
 */
public final class XmlParser
{
  public XmlParser( Reader reader)
  {
    this.factory = new ModelObjectFactory();
    this.reader = reader;
    this.buffer = new char[ 4096];
    this.mark = -1;
    
    // default entities
    entities = new HashMap<String, String>();
    entities.put( "lt", "<");
    entities.put( "gt", ">");
    entities.put( "amp", "&");
    entities.put( "quot", "\"");
    entities.put( "apos", "'");
  }
  
  /**
   * Returns the entity with the specified name.
   * @param name The EntityRef name.
   * @return Returns 0 or the entity with the specified name.
   */
  public String lookupEntity( String name)
  {
    return entities.get( name);
  }
  
  /**
   * Parse the document and return the document node.
   * @return Returns the document node.
   */
  public final IModelObject parse() throws IOException, ParseException
  {
    IModelObject node = factory.createObject( null, "document");
    while( parseContent( node, null));
    return node;
  }
  
  /**
   * Parse an element declaration (assumes '<' already read).
   * @return Returns null or the parsed element.
   */
  private final IModelObject parseElement() throws IOException, ParseException
  {
    StringBuilder name = new StringBuilder();
    if ( !parseName( name)) return null;
    
    IModelObject element = factory.createObject( null, name.toString());
    StringBuilder text = new StringBuilder();
    
    char c = readSkip();
    if ( c == '/')
    {
      c = readSkip();
      if ( c != '>')
      {
        throw createException(  "Expected > character.");
      }
      return element;
    }
    else if ( c == '>')
    {
      while( parseContent( element, text));
    }
    else
    {
      offset--;
      while( parseAttributes( element));
      
      c = readSkip();
      if ( c == '/')
      {
        c = readSkip();
        if ( c != '>')
        {
          throw createException(  "Expected > character.");
        }
        return element;
      }
      else if ( c != '>')
      {
        throw createException(  "Expected end of element tag.");
      }
      
      while( parseContent( element, text));
    }
    
    // consume / belonging to end tag
    c = readSkip();
    if ( c != '/')
    {
      throw createException(  "Illegal character.");
    }
    
    // parse end tag (assumes </ already read)
    if ( !parseExactly( element.getType()))
    {
      throw createException( String.format( "Expected \"%s\" end tag.", element.getType()));
    }
    
    c = readSkip();
    if ( c != '>')
    {
      throw createException(  "Illegal character.");
    }
    
    element.setValue( text.toString());
    return element;
  }
  
  /**
   * Parse element attributes from the stream.
   * @param element The element.
   * @return Returns true if the parse was successful.
   */
  private final boolean parseAttributes( IModelObject element) throws IOException, ParseException
  {
    char c = readSkip(); offset--;
    
    StringBuilder attrName = new StringBuilder();
    if ( !parseName( attrName)) { offset--; return false;}
    
    c = read();
    if ( c != '=')
    {
      throw createException(  "Expected = character.");
    }
    
    StringBuilder attrValue = new StringBuilder();
    char q = read();
    if ( q != '\'' && q != '"')
    {
      throw createException(  "Expected ' or \" character.");
    }
    
    c = read();
    while( c != q)
    {
      if ( c == '<')
      {
        throw createException(  "Illegal character.");
      }
      else if ( c == '&')
      {
        parseReference( attrValue);
      }
      else
      {
        attrValue.append( c);
      }
      
      c = read();
    }
    
    element.setAttribute( attrName.toString(), attrValue.toString());
    return true;
  }
  
  /**
   * Parse some content of an element.
   * @param element The element whose content will be parsed.
   * @param text The text accumulator.
   * @return Returns false at the end of the element content.
   */
  private boolean parseContent( IModelObject element, StringBuilder text) throws IOException, ParseException
  {
    char c = read(); if ( eoi) return false;
    while( c != '<' && !eoi)
    {
      if ( c == '&')
      {
        parseReference( text);
      }
      else
      {
        if ( text != null) text.append( c);
      }
      c = read();
    }
    
    c = read(); 
    if ( eoi) return false;
    
    if ( c == '/')
    {
      offset--;
      return false;
    }
    else if ( c == '!')
    {
      c = read();
      switch( c)
      {
        case '-': if ( parseComment( element)) return true;
        case '[': if ( parseCDATA( text)) return true;
        case 'D': if ( parseDOCTYPE()) return true;
        case 'E': if ( parseENTITY()) return true;
        default: throw createException(  "Illegal declaration.");
      }
    }
    else if ( c == '?')
    {
      if ( parsePI( element)) return true;
      throw createException(  "Illegal declaration.");
    }
    else
    {
      offset--;
      IModelObject child = parseElement();
      if ( child != null)
      {
        if ( recordTextPosition) child.setAttribute( "!position", text.length());
        element.addChild( child);
        return true;
      }
      return false;
    }
  }
  
  /**
   * Parse an XML Reference and append it to the specified result.
   * @param result The result.
   */
  private void parseReference( StringBuilder result) throws IOException, ParseException
  {
    char c = read();
    if ( c == '#')
    {
      StringBuilder sb = new StringBuilder();
      c = read();
      if ( c == 'x')
      {
        c = read();
        while( c != ';')
        {
          if ( !isHexDigit( c))
          {
            throw createException(  "Expected hex digit character.");
          }
          sb.append( c);
        }

        result.append( (char)Integer.parseInt( sb.toString(), 16));
      }
      else
      {
        while( c != ';')
        {
          if ( !isDigit( c))
          {
            throw createException(  "Expected digit character.");
          }
          sb.append( c);
        }
        result.append( (char)Integer.parseInt( sb.toString()));
      }
    }
    else
    {
      StringBuilder sb = new StringBuilder();
      while( c != ';' && !eoi)
      {
        sb.append( c);
        c = read();
      }
      
      String entity = lookupEntity( sb.toString());
      if ( entity == null) throw createException(  "Undefined entity.");
      
      result.append( entity);
    }
  }
  
  /**
   * Parse a ENTITY declaration (assumes '<' already read).
   * @return Returns true if the parse was successful.
   */
  private final boolean parseENTITY() throws IOException, ParseException
  {
    if ( !parseExactly( openENTITYChars)) return false;
    
    char c = readSkip(); if ( eoi) return false;
    offset--;
    
    // entity name
    StringBuilder sb = new StringBuilder();
    if ( !parseName( sb)) return false;
    
    String name = sb.toString();
    
    // entity value
    sb.setLength( 0);
    char q = readSkip();
    if ( q != '\'' && q != '"')
    {
      throw createException(  "Expected ' or \" character.");
    }
    
    c = read();
    while( c != q)
    {
      if ( c == '<')
      {
        throw createException(  "Illegal character.");
      }
      else if ( c == '&')
      {
        parseReference( sb);
      }
      else
      {
        sb.append( c);
      }
      
      c = read();
    }
    
    entities.put( name, sb.toString());
    
    return true;
  }
  
  /**
   * Parse a DOCTYPE declaration (assumes '<' already read).
   * @return Returns true if the parse was successful.
   */
  private final boolean parseDOCTYPE() throws IOException, ParseException
  {
    if ( !parseExactly( openDOCTYPEChars)) return false;
    char c = read(); if ( eoi) return false;
    while( c != '>')
    {
      c = read(); if ( eoi) return false;
    }
    return true;
  }
  
  /**
   * Parse a CDATA section (assumes '<' already read).
   * @param result The content of the CDATA section.
   * @return Returns true if the parse was successful.
   */
  private final boolean parseCDATA( StringBuilder result) throws IOException, ParseException
  {
    if ( !parseExactly( openCDATAChars)) return false;
    
    char c = read(); if ( eoi) return false;
    while( isChar( c) && !eoi)
    {
      if ( c == ']' && consume( "]>")) break;
      result.append( c);
      c = read();
    }
    
    offset--;
    return true;
  }
  
  /**
   * Parse a processing instruction (assumes "<?" already read).
   * @param parent The parent of the processing-instruction.
   * @return Returns true if parse is successful.
   */
  private final boolean parsePI( IModelObject parent) throws IOException, ParseException
  {
    StringBuilder sb = new StringBuilder();
    sb.append( '?');
    if ( !parseName( sb)) 
    {
      throw createException( "Expected processing-instruction name.");
    }
    
    IModelObject pi = factory.createObject( null, sb.toString());
    sb.setLength( 0);
    
    char c = readSkip(); if ( eoi) return false;
    while( isChar( c) && !eoi)
    {
      if ( c == '?')
      {
        c = read(); if ( eoi) return false;
        if ( c == '>') break;
        sb.append( '?');
        sb.append( c);
      }
      else
      {
        sb.append( c);
      }
      c = read();
    }
    
    pi.setValue( sb);
    parent.addChild( pi);
    
    return true;
  }
  
  /**
   * Parse a comment (assumes '<' already read).
   * @param parent The parent element containing the comment.
   * @return Returns true if parse is successful.
   */
  private final boolean parseComment( IModelObject parent) throws IOException, ParseException
  {
    char c = read(); if ( eoi) return false;
    if ( c != '-' || eoi) return false;
    
    StringBuilder body = new StringBuilder();
    body.append( "!--");
    
    char c0 = 0;
    char c1 = read(); if ( eoi) return false;
    while( isChar( c1) && !eoi)
    {
      body.append( c1);
      if ( c1 == '-' && c0 == '-') break;
      c0 = c1; c1 = read();
    }

    c1 = read();
    if ( c1 != '>') return false;
    
    
    IModelObject comment = factory.createObject( null, body.toString());
    parent.addChild( comment);
    
    return true;
  }
  
  /**
   * Parse exactly the specified characters from the stream.
   * @param chars The characters.
   * @return Returns true if the parse was successful.
   */
  private final boolean parseExactly( String chars) throws IOException, ParseException
  {
    char c = read();
    if ( eoi || c != chars.charAt( 0)) { offset--; return false;}
    
    for( int i=1; i<chars.length(); i++)
    {
      c = read();
      if ( eoi || c != chars.charAt( i))
      {
        throw createException(  "Illegal character.");
      }
    }
    return true;
  }

  /**
   * Returns true if the stream contains the specified characters and consumes them.
   * @param chars The characters.
   * @return Returns true if the stream contains the specified characters.
   */
  private final boolean consume( String chars) throws IOException
  {
    mark();
    
    char c = read();
    for( int i=0; i<chars.length() && !eoi; i++)
    {
      if ( c != chars.charAt( i)) { reset(); return false;}
      c = read();
    }
    
    return true;
  }
  
  /**
   * Returns true if an XML NMToken could be parsed.
   * @param result The result.
   * @return Returns true if parse was succesful.
   */
  @SuppressWarnings("unused")
  private final boolean parseNMToken( StringBuilder result) throws IOException
  {
    char c = read(); if ( eoi) return false;
    if ( isNameChar( c))
    {
      result.append( c);
      
      c = read(); if ( eoi) return true;
      while( isNameChar( c))
      {
        result.append( c);
      }
      
      offset--;
      return true;
    }
    return false;
  }
  
  /**
   * Returns true if a name could be parsed.
   * @param result The result.
   * @return Returns true if parse was succesful.
   */
  private final boolean parseName( StringBuilder result) throws IOException
  {
    char c = read(); if ( eoi) return false;
    if ( isLetter( c) || c == '_' || c == ':')
    {
      result.append( c);
      
      c = read(); if ( eoi) return true;
      while( isNameChar( c) && !eoi)
      {
        result.append( c); 
        c = read();
      }
      
      offset--;
      return true;
    }
    return false;
  }
  
  /**
   * Read a non-whitespace character from the stream and set EOI if necessary.
   * @return Returns the non-whitespace character that was read.
   */
  private final char readSkip() throws IOException
  {
    char c = read();
    while( !eoi && isWhitespace( c))
    {
      c = read();
    }
    return c;
  }
  
  /**
   * Mark the current position in the stream.
   */
  private final void mark()
  {
    mark = offset;
  }
  
  /**
   * Reset the buffer to the mark position.
   */
  private final void reset() throws IOException
  {
    if ( mark == -1) throw new IOException( "Buffer overrun.");
    offset = mark;
  }
  
  /**
   * Read a character from the stream, normalize line breaks, and set EOI if necessary.
   * @return Returns the character read.
   */
  private final char read() throws IOException
  {
    char c = readRaw();
    
    // normalize line breaks to 0x0A
    if ( c == 0x0D)
    {
      c = readRaw();
      if ( c != 0x0A)
      {
        offset--;
        return 0x0A;
      }
    }
    
    return c;
  }
  
  /**
   * Read a raw character from the stream and set EOI if necessary.
   * @return Returns the character read.
   */
  private final char readRaw() throws IOException
  {
    if ( offset >= length)
    {
      if ( mark < 0)
      {
        // preserve one character from previous buffer
        if ( length > 0) buffer[ 0] = buffer[ length-1];
        
        // read next buffer
        length = reader.read( buffer, 1, buffer.length - 1) + 1;
        
        // end of input
        if ( length <= 0) 
        {
          eoi = true;
          return 0;
        }
        
        offset = 1;
        count += length;
      }
      else if ( mark > 0)
      {
        // preserve region of buffer beginning with mark
        System.arraycopy( buffer, mark, buffer, 0, length - mark);
        
        // read next buffer
        int read = reader.read( buffer, length, buffer.length - length);
        
        // end of input
        if ( read < 0) 
        {
          eoi = true;
          return 0;
        }
        
        offset -= mark;
        mark = 0;
        count += read;
      }
      else
      {
        // clear mark
        mark = -1;
      }
    }
    
    return buffer[ offset++];
  }

  /**
   * Create a ParseException with the specified message.
   * @param message The message.
   * @return Returns the exception.
   */
  private final ParseException createException( String message)
  {
    return new ParseException( message, (count - length + offset));
  }
  
  /**
   * Returns true if the specified character is an XML NameChar.
   * @param c The character.
   * @return Returns true if the specified character is the correct class.
   */
  private final static boolean isNameChar( char c)
  {
    switch( c)
    {
      case '.':
      case '-':
      case '_':
      case ':':
        return true;
    }
    
    if ( isLetter( c) || isDigit( c)) return true;
    if ( isCombiningChar( c) || isExtender( c)) return true;
    
    return false;
  }
  
  /**
   * Returns true if the specified character is an XML Whitespace.
   * @param c The character.
   * @return Returns true if the specified character is the correct class.
   */
  private final static boolean isWhitespace( char c)
  {
    switch( c)
    {
      case 0x0009:
      case 0x000a:
      case 0x000d:
      case 0x0020:
        return true;
    }
    return false;
  }
  
  /**
   * Returns true if the specified character is an XML Char.
   * @param c The character.
   * @return Returns true if the specified character is the correct class.
   */
  private final static boolean isChar( char c)
  {
    if (c == 0x0009) return true;
    if (c == 0x000a) return true;
    if (c == 0x000d) return true;
    if (c >= 0x0020 && c <= 0xd7ff) return true;
    if (c >= 0xe000 && c <= 0xfffd) return true;
    if (c >= 0x10000 && c <= 0x10ffff) return true;
    return false;
  }
  
  /**
   * Returns true if the specified character is an XML Letter.
   * @param c The character.
   * @return Returns true if the specified character is the correct class.
   */
  private final static boolean isLetter( char c)
  {
    return isBaseChar( c) || isIdeographic( c);
  }
  
  /**
   * Returns true if the specified character is an XML hex digit.
   * @param c The character.
   * @return Returns true if the specified character is the correct class.
   */
  private final static boolean isHexDigit( char c)
  {
    if ( c < '0' || c > 'f') return false;
    if ( c > '9' && c < 'A') return false;
    if ( c > 'F' && c < 'a') return false;
    return true;
  }
  
  /**
   * Returns true if the specified character is an XML Digit.
   * @param c The character.
   * @return Returns true if the specified character is the correct class.
   */
  private final static boolean isDigit( char c)
  {
    if (c >= 0x0030 && c <= 0x0039) return true;
    
    if (c < 0x0660 || c > 0x0f29) return false;
    
    if (c >= 0x0660 && c <= 0x0669) return true;
    if (c >= 0x06F0 && c <= 0x06F9) return true;
    if (c >= 0x0966 && c <= 0x096F) return true;
    if (c >= 0x09E6 && c <= 0x09EF) return true;
    if (c >= 0x0A66 && c <= 0x0A6F) return true;
    if (c >= 0x0AE6 && c <= 0x0AEF) return true;
    if (c >= 0x0B66 && c <= 0x0B6F) return true;
    if (c >= 0x0BE7 && c <= 0x0BEF) return true;
    if (c >= 0x0C66 && c <= 0x0C6F) return true;
    if (c >= 0x0CE6 && c <= 0x0CEF) return true;
    if (c >= 0x0D66 && c <= 0x0D6F) return true;
    if (c >= 0x0E50 && c <= 0x0E59) return true;
    if (c >= 0x0ED0 && c <= 0x0ED9) return true;
    if (c >= 0x0F20 && c <= 0x0F29) return true;
    return false;
  }
  
  /**
   * Returns true if the specified character is an XML Extender.
   * @param c The character.
   * @return Returns true if the specified character is the correct class.
   */
  private final static boolean isExtender( char c)
  {
    if (c < 0x00b7) return false;
    
    if (c == 0x00B7) return true;
    if (c == 0x02D0) return true;
    if (c == 0x02D1) return true;
    if (c == 0x0387) return true;
    if (c == 0x0640) return true;
    if (c == 0x0E46) return true;
    if (c == 0x0EC6) return true;
    if (c == 0x3005) return true;
    if (c >= 0x3031 && c <= 0x3035) return true;
    if (c >= 0x309D && c <= 0x309E) return true;
    if (c >= 0x30FC && c <= 0x30FE) return true;
    
    return false;
  }  
  
  /**
   * Returns true if the specified character is an XML CombiningChar.
   * @param c The character.
   * @return Returns true if the specified character is the correct class.
   */
  private final static boolean isCombiningChar( char c)
  {
    if (c <= 0x05c4)
    {
      if (c < 0x0300) return false;
      if (c >= 0x0300 && c <= 0x0345) return true;
      if (c >= 0x0360 && c <= 0x0361) return true;
      if (c >= 0x0483 && c <= 0x0486) return true;
      if (c >= 0x0591 && c <= 0x05A1) return true;
      if (c >= 0x05A3 && c <= 0x05B9) return true;
      if (c >= 0x05BB && c <= 0x05BD) return true;
      if (c == 0x05BF) return true;
      if (c >= 0x05C1 && c <= 0x05C2) return true;
      if (c == 0x05C4) return true;
    }
    else if (c <= 0x09e3)
    {
      if (c < 0x064b) return false;
      if (c >= 0x064B && c <= 0x0652) return true;
      if (c == 0x0670) return true;
      if (c >= 0x06D6 && c <= 0x06DC) return true;
      if (c >= 0x06DD && c <= 0x06DF) return true;
      if (c >= 0x06E0 && c <= 0x06E4) return true;
      if (c >= 0x06E7 && c <= 0x06E8) return true;
      if (c >= 0x06EA && c <= 0x06ED) return true;
      if (c >= 0x0901 && c <= 0x0903) return true;
      if (c == 0x093C) return true;
      if (c >= 0x093E && c <= 0x094C) return true;
      if (c == 0x094D) return true;
      if (c >= 0x0951 && c <= 0x0954) return true;
      if (c >= 0x0962 && c <= 0x0963) return true;
      if (c >= 0x0981 && c <= 0x0983) return true;
      if (c == 0x09BC) return true;
      if (c == 0x09BE) return true;
      if (c == 0x09BF) return true;
      if (c >= 0x09C0 && c <= 0x09C4) return true;
      if (c >= 0x09C7 && c <= 0x09C8) return true;
      if (c >= 0x09CB && c <= 0x09CD) return true;
      if (c == 0x09D7) return true;
      if (c >= 0x09E2 && c <= 0x09E3) return true;
    }
    else if (c <= 0x0acd)
    {
      if (c < 0x0a02) return false;
      if (c == 0x0A02) return true;
      if (c == 0x0A3C) return true;
      if (c == 0x0A3E) return true;
      if (c == 0x0A3F) return true;
      if (c >= 0x0A40 && c <= 0x0A42) return true;
      if (c >= 0x0A47 && c <= 0x0A48) return true;
      if (c >= 0x0A4B && c <= 0x0A4D) return true;
      if (c >= 0x0A70 && c <= 0x0A71) return true;
      if (c >= 0x0A81 && c <= 0x0A83) return true;
      if (c == 0x0ABC) return true;
      if (c >= 0x0ABE && c <= 0x0AC5) return true;
      if (c >= 0x0AC7 && c <= 0x0AC9) return true;
      if (c >= 0x0ACB && c <= 0x0ACD) return true;
    }
    else if (c <= 0x0cd6)
    {
      if (c < 0x0b01) return false;
      if (c >= 0x0B01 && c <= 0x0B03) return true;
      if (c == 0x0B3C) return true;
      if (c >= 0x0B3E && c <= 0x0B43) return true;
      if (c >= 0x0B47 && c <= 0x0B48) return true;
      if (c >= 0x0B4B && c <= 0x0B4D) return true;
      if (c >= 0x0B56 && c <= 0x0B57) return true;
      if (c >= 0x0B82 && c <= 0x0B83) return true;
      if (c >= 0x0BBE && c <= 0x0BC2) return true;
      if (c >= 0x0BC6 && c <= 0x0BC8) return true;
      if (c >= 0x0BCA && c <= 0x0BCD) return true;
      if (c == 0x0BD7) return true;
      if (c >= 0x0C01 && c <= 0x0C03) return true;
      if (c >= 0x0C3E && c <= 0x0C44) return true;
      if (c >= 0x0C46 && c <= 0x0C48) return true;
      if (c >= 0x0C4A && c <= 0x0C4D) return true;
      if (c >= 0x0C55 && c <= 0x0C56) return true;
      if (c >= 0x0C82 && c <= 0x0C83) return true;
      if (c >= 0x0CBE && c <= 0x0CC4) return true;
      if (c >= 0x0CC6 && c <= 0x0CC8) return true;
      if (c >= 0x0CCA && c <= 0x0CCD) return true;
      if (c >= 0x0CD5 && c <= 0x0CD6) return true;
    }
    else if (c <= 0x0fb9)
    {
      if (c < 0x0d02) return false;
      if (c >= 0x0D02 && c <= 0x0D03) return true;
      if (c >= 0x0D3E && c <= 0x0D43) return true;
      if (c >= 0x0D46 && c <= 0x0D48) return true;
      if (c >= 0x0D4A && c <= 0x0D4D) return true;
      if (c == 0x0D57) return true;
      if (c == 0x0E31) return true;
      if (c >= 0x0E34 && c <= 0x0E3A) return true;
      if (c >= 0x0E47 && c <= 0x0E4E) return true;
      if (c == 0x0EB1) return true;
      if (c >= 0x0EB4 && c <= 0x0EB9) return true;
      if (c >= 0x0EBB && c <= 0x0EBC) return true;
      if (c >= 0x0EC8 && c <= 0x0ECD) return true;
      if (c >= 0x0F18 && c <= 0x0F19) return true;
      if (c == 0x0F35) return true;
      if (c == 0x0F37) return true;
      if (c == 0x0F39) return true;
      if (c == 0x0F3E) return true;
      if (c == 0x0F3F) return true;
      if (c >= 0x0F71 && c <= 0x0F84) return true;
      if (c >= 0x0F86 && c <= 0x0F8B) return true;
      if (c >= 0x0F90 && c <= 0x0F95) return true;
      if (c == 0x0F97) return true;
      if (c >= 0x0F99 && c <= 0x0FAD) return true;
      if (c >= 0x0FB1 && c <= 0x0FB7) return true;
      if (c == 0x0FB9) return true;
    }
    else if ( c <= 0x309a)
    {
      if (c < 0x20d0) return false;
      if (c >= 0x20D0 && c <= 0x20DC) return true;
      if (c == 0x20E1) return true;
      if (c >= 0x302A && c <= 0x302F) return true;
      if (c == 0x3099) return true;
      if (c == 0x309A) return true;
    }
    return false;
  }
  
  /**
   * Returns true if the specified character is an XML Ideographic.
   * @param c The character.
   * @return Returns true if the specified character is the correct class.
   */
  private final static boolean isIdeographic( char c)
  {
    if (c < 0x3007) return false;
    if (c >= 0x4E00 && c <= 0x9FA5) return true;
    if (c == 0x3007) return true;
    if (c >= 0x3021 && c <= 0x3029) return true;
    return false;
  }
  
  /**
   * Returns true if the specified character is an XML BaseChar.
   * @param c The character.
   * @return Returns true if the specified character is the correct class.
   */
  private final static boolean isBaseChar( char c)
  {
    if (c <= 0x0100)
    {
      if (c < 0x0041) return false;
      if (c >= 0x0041 && c <= 0x005A) return true;
      if (c >= 0x0061 && c <= 0x007A) return true;
      if (c >= 0x00C0 && c <= 0x00D6) return true;
      if (c >= 0x00D8 && c <= 0x00F6) return true;
      if (c >= 0x00F8) return true;
    }
    else if (c <= 0x0217)
    {
      if (c >= 0x0100 && c <= 0x0131) return true;
      if (c >= 0x0134 && c <= 0x013E) return true;
      if (c >= 0x0141 && c <= 0x0148) return true;
      if (c >= 0x014A && c <= 0x017E) return true;
      if (c >= 0x0180 && c <= 0x01C3) return true;
      if (c >= 0x01CD && c <= 0x01F0) return true;
      if (c >= 0x01F4 && c <= 0x01F5) return true;
      if (c >= 0x01FA) return true;
    }
    else if (c <= 0x03f3)
    {
      if (c < 0x0250) return false;
      if (c >= 0x0250 && c <= 0x02A8) return true;
      if (c >= 0x02BB && c <= 0x02C1) return true;
      if (c == 0x0386) return true;
      if (c >= 0x0388 && c <= 0x038A) return true;
      if (c == 0x038C) return true;
      if (c >= 0x038E && c <= 0x03A1) return true;
      if (c >= 0x03A3 && c <= 0x03CE) return true;
      if (c >= 0x03D0 && c <= 0x03D6) return true;
      if (c == 0x03DA) return true;
      if (c == 0x03DC) return true;
      if (c == 0x03DE) return true;
      if (c == 0x03E0) return true;
      if (c >= 0x03E2) return true;
    }
    else if (c <= 0x04f9)
    {
      if (c < 0x0401) return false;
      if (c >= 0x0401 && c <= 0x040C) return true;
      if (c >= 0x040E && c <= 0x044F) return true;
      if (c >= 0x0451 && c <= 0x045C) return true;
      if (c >= 0x045E && c <= 0x0481) return true;
      if (c >= 0x0490 && c <= 0x04C4) return true;
      if (c >= 0x04C7 && c <= 0x04C8) return true;
      if (c >= 0x04CB && c <= 0x04CC) return true;
      if (c >= 0x04D0 && c <= 0x04EB) return true;
      if (c >= 0x04EE && c <= 0x04F5) return true;
      if (c >= 0x04F8) return true;
    }
    else if (c <= 0x06e6)
    {
      if (c < 0x0531) return false;
      if (c >= 0x0531 && c <= 0x0556) return true;
      if (c == 0x0559) return true;
      if (c >= 0x0561 && c <= 0x0586) return true;
      if (c >= 0x05D0 && c <= 0x05EA) return true;
      if (c >= 0x05F0 && c <= 0x05F2) return true;
      if (c >= 0x0621 && c <= 0x063A) return true;
      if (c >= 0x0641 && c <= 0x064A) return true;
      if (c >= 0x0671 && c <= 0x06B7) return true;
      if (c >= 0x06BA && c <= 0x06BE) return true;
      if (c >= 0x06C0 && c <= 0x06CE) return true;
      if (c >= 0x06D0 && c <= 0x06D3) return true;
      if (c == 0x06D5) return true;
      if (c >= 0x06E5) return true;
    }
    else if (c <= 0x09f1)
    {
      if (c < 0x0905) return false;
      if (c >= 0x0905 && c <= 0x0939) return true;
      if (c == 0x093D) return true;
      if (c >= 0x0958 && c <= 0x0961) return true;
      if (c >= 0x0985 && c <= 0x098C) return true;
      if (c >= 0x098F && c <= 0x0990) return true;
      if (c >= 0x0993 && c <= 0x09A8) return true;
      if (c >= 0x09AA && c <= 0x09B0) return true;
      if (c == 0x09B2) return true;
      if (c >= 0x09B6 && c <= 0x09B9) return true;
      if (c >= 0x09DC && c <= 0x09DD) return true;
      if (c >= 0x09DF && c <= 0x09E1) return true;
      if (c >= 0x09F0) return true;
    }
    else if ( c <= 0x0ae0)
    {
      if (c < 0x0a05) return false;
      if (c >= 0x0A05 && c <= 0x0A0A) return true;
      if (c >= 0x0A0F && c <= 0x0A10) return true;
      if (c >= 0x0A13 && c <= 0x0A28) return true;
      if (c >= 0x0A2A && c <= 0x0A30) return true;
      if (c >= 0x0A32 && c <= 0x0A33) return true;
      if (c >= 0x0A35 && c <= 0x0A36) return true;
      if (c >= 0x0A38 && c <= 0x0A39) return true;
      if (c >= 0x0A59 && c <= 0x0A5C) return true;
      if (c == 0x0A5E) return true;
      if (c >= 0x0A72 && c <= 0x0A74) return true;
      if (c >= 0x0A85 && c <= 0x0A8B) return true;
      if (c == 0x0A8D) return true;
      if (c >= 0x0A8F && c <= 0x0A91) return true;
      if (c >= 0x0A93 && c <= 0x0AA8) return true;
      if (c >= 0x0AAA && c <= 0x0AB0) return true;
      if (c >= 0x0AB2 && c <= 0x0AB3) return true;
      if (c >= 0x0AB5 && c <= 0x0AB9) return true;
      if (c == 0x0ABD) return true;
      if (c == 0x0AE0) return true;
    }
    else if (c <= 0x0bb9)
    {
      if (c < 0x0b05) return false;
      if (c >= 0x0B05 && c <= 0x0B0C) return true;
      if (c >= 0x0B0F && c <= 0x0B10) return true;
      if (c >= 0x0B13 && c <= 0x0B28) return true;
      if (c >= 0x0B2A && c <= 0x0B30) return true;
      if (c >= 0x0B32 && c <= 0x0B33) return true;
      if (c >= 0x0B36 && c <= 0x0B39) return true;
      if (c == 0x0B3D) return true;
      if (c >= 0x0B5C && c <= 0x0B5D) return true;
      if (c >= 0x0B5F && c <= 0x0B61) return true;
      if (c >= 0x0B85 && c <= 0x0B8A) return true;
      if (c >= 0x0B8E && c <= 0x0B90) return true;
      if (c >= 0x0B92 && c <= 0x0B95) return true;
      if (c >= 0x0B99 && c <= 0x0B9A) return true;
      if (c == 0x0B9C) return true;
      if (c >= 0x0B9E && c <= 0x0B9F) return true;
      if (c >= 0x0BA3 && c <= 0x0BA4) return true;
      if (c >= 0x0BA8 && c <= 0x0BAA) return true;
      if (c >= 0x0BAE && c <= 0x0BB5) return true;
      if (c >= 0x0BB7) return true;
    }
    else if (c <= 0x0ce1)
    {
      if (c < 0x0c05) return false;
      if (c >= 0x0C05 && c <= 0x0C0C) return true;
      if (c >= 0x0C0E && c <= 0x0C10) return true;
      if (c >= 0x0C12 && c <= 0x0C28) return true;
      if (c >= 0x0C2A && c <= 0x0C33) return true;
      if (c >= 0x0C35 && c <= 0x0C39) return true;
      if (c >= 0x0C60 && c <= 0x0C61) return true;
      if (c >= 0x0C85 && c <= 0x0C8C) return true;
      if (c >= 0x0C8E && c <= 0x0C90) return true;
      if (c >= 0x0C92 && c <= 0x0CA8) return true;
      if (c >= 0x0CAA && c <= 0x0CB3) return true;
      if (c >= 0x0CB5 && c <= 0x0CB9) return true;
      if (c == 0x0CDE) return true;
      if (c >= 0x0CE0) return true;
    }
    else if (c <= 0x0d61)
    {
      if (c < 0x0d05) return false;
      if (c >= 0x0D05 && c <= 0x0D0C) return true;
      if (c >= 0x0D0E && c <= 0x0D10) return true;
      if (c >= 0x0D12 && c <= 0x0D28) return true;
      if (c >= 0x0D2A && c <= 0x0D39) return true;
      if (c >= 0x0D60 && c <= 0x0D61) return true;
    }
    else if (c <= 0x0ec4)
    {
      if (c < 0x0e01) return false;
      if (c >= 0x0E01 && c <= 0x0E2E) return true;
      if (c == 0x0E30) return true;
      if (c >= 0x0E32 && c <= 0x0E33) return true;
      if (c >= 0x0E40 && c <= 0x0E45) return true;
      if (c >= 0x0E81 && c <= 0x0E82) return true;
      if (c == 0x0E84) return true;
      if (c >= 0x0E87 && c <= 0x0E88) return true;
      if (c == 0x0E8A) return true;
      if (c == 0x0E8D) return true;
      if (c >= 0x0E94 && c <= 0x0E97) return true;
      if (c >= 0x0E99 && c <= 0x0E9F) return true;
      if (c >= 0x0EA1 && c <= 0x0EA3) return true;
      if (c == 0x0EA5) return true;
      if (c == 0x0EA7) return true;
      if (c >= 0x0EAA && c <= 0x0EAB) return true;
      if (c >= 0x0EAD && c <= 0x0EAE) return true;
      if (c == 0x0EB0) return true;
      if (c >= 0x0EB2 && c <= 0x0EB3) return true;
      if (c == 0x0EBD) return true;
      if (c >= 0x0EC0) return true;
    }
    else if (c <= 0x11f9)
    {
      if (c < 0x0f40) return false;
      if (c >= 0x0F40 && c <= 0x0F47) return true;
      if (c >= 0x0F49 && c <= 0x0F69) return true;
      if (c >= 0x10A0 && c <= 0x10C5) return true;
      if (c >= 0x10D0 && c <= 0x10F6) return true;
      if (c == 0x1100) return true;
      if (c >= 0x1102 && c <= 0x1103) return true;
      if (c >= 0x1105 && c <= 0x1107) return true;
      if (c == 0x1109) return true;
      if (c >= 0x110B && c <= 0x110C) return true;
      if (c >= 0x110E && c <= 0x1112) return true;
      if (c == 0x113C) return true;
      if (c == 0x113E) return true;
      if (c == 0x1140) return true;
      if (c == 0x114C) return true;
      if (c == 0x114E) return true;
      if (c == 0x1150) return true;
      if (c >= 0x1154 && c <= 0x1155) return true;
      if (c == 0x1159) return true;
      if (c >= 0x115F && c <= 0x1161) return true;
      if (c == 0x1163) return true;
      if (c == 0x1165) return true;
      if (c == 0x1167) return true;
      if (c == 0x1169) return true;
      if (c >= 0x116D && c <= 0x116E) return true;
      if (c >= 0x1172 && c <= 0x1173) return true;
      if (c == 0x1175) return true;
      if (c == 0x119E) return true;
      if (c == 0x11A8) return true;
      if (c == 0x11AB) return true;
      if (c >= 0x11AE && c <= 0x11AF) return true;
      if (c >= 0x11B7 && c <= 0x11B8) return true;
      if (c == 0x11BA) return true;
      if (c >= 0x11BC && c <= 0x11C2) return true;
      if (c == 0x11EB) return true;
      if (c == 0x11F0) return true;
      if (c == 0x11F9) return true;
    }
    else if (c <= 0x1ffc)
    {
      if (c < 0x01e00) return false;
      if (c >= 0x1E00 && c <= 0x1E9B) return true;
      if (c >= 0x1EA0 && c <= 0x1EF9) return true;
      if (c >= 0x1F00 && c <= 0x1F15) return true;
      if (c >= 0x1F18 && c <= 0x1F1D) return true;
      if (c >= 0x1F20 && c <= 0x1F45) return true;
      if (c >= 0x1F48 && c <= 0x1F4D) return true;
      if (c >= 0x1F50 && c <= 0x1F57) return true;
      if (c == 0x1F59) return true;
      if (c == 0x1F5B) return true;
      if (c == 0x1F5D) return true;
      if (c >= 0x1F5F && c <= 0x1F7D) return true;
      if (c >= 0x1F80 && c <= 0x1FB4) return true;
      if (c >= 0x1FB6 && c <= 0x1FBC) return true;
      if (c == 0x1FBE) return true;
      if (c >= 0x1FC2 && c <= 0x1FC4) return true;
      if (c >= 0x1FC6 && c <= 0x1FCC) return true;
      if (c >= 0x1FD0 && c <= 0x1FD3) return true;
      if (c >= 0x1FD6 && c <= 0x1FDB) return true;
      if (c >= 0x1FE0 && c <= 0x1FEC) return true;
      if (c >= 0x1FF2 && c <= 0x1FF4) return true;
      if (c >= 0x1FF6) return true;
    }
    else if (c <= 0xd7a3)
    {
      if (c < 0x2126) return false;
      if (c == 0x2126) return true;
      if (c >= 0x212A && c <= 0x212B) return true;
      if (c == 0x212E) return true;
      if (c >= 0x2180 && c <= 0x2182) return true;
      if (c >= 0x3041 && c <= 0x3094) return true;
      if (c >= 0x30A1 && c <= 0x30FA) return true;
      if (c >= 0x3105 && c <= 0x312C) return true;
      if (c >= 0xAC00) return true;
    }
    return false;
  }

  private final static String openCDATAChars = "CDATA[";
  private final static String openDOCTYPEChars = "OCTYPE";
  private final static String openENTITYChars = "NTITY";

  private IModelObjectFactory factory;
  private Map<String, String> entities;
  private Reader reader;
  private char[] buffer;
  private int offset;
  private int length;
  private int mark;
  private int count;
  private boolean eoi;
  
  // flags
  private boolean recordTextPosition;
  
  public static void main( String[] args) throws Exception
  {
    File folder = new File( ".");
    File[] files = folder.listFiles();
    for( File file: files)
    {
      if ( !file.getName().endsWith( "test.xml")) continue;
      
      FileReader reader = new FileReader( file);
      System.out.println( file);
      try
      {
        XmlParser parser = new XmlParser( reader);
        ModelObject element = (ModelObject)parser.parse();
        System.out.println( element.toXml());
      }
      catch( ParseException e)
      {
        System.err.println( e.getMessage());
      }
    }
  }
}
