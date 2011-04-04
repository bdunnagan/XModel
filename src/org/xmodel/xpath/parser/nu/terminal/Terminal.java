package org.xmodel.xpath.parser.nu.terminal;

import java.io.IOException;

import org.xmodel.xpath.parser.nu.Stream;

public final class Terminal
{
  public final static boolean readQName( Stream stream, StringBuilder result) throws IOException
  {
    if ( !readNCName( stream, result)) return false;
    
    int c = stream.read();
    if ( c != ':') return true;
    result.append( c);
    
    if ( !readNCName( stream, result)) return false;
    
    return true;
  }
  
  public final static boolean readWhitespace( Stream stream, StringBuilder result) throws IOException
  {
    boolean found = false;
    int c = stream.read();
    while( (c == 0x20) || (c == 0x9) || (c == 0xD) || (c == 0xA))
    {
      result.append( c);
      c = stream.read();
      found = true;
    }
    return found;
  }
  
  public final static boolean readName( Stream stream, StringBuilder result) throws IOException
  {
    boolean found = false;
    
    // NameStartChar
    int c = stream.read();
    if ( (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_') || (c == ':') ||  
         (c >= 0xC0 && c <= 0xD6) || (c >= 0xD8 && c <= 0xF6) || (c >= 0xF8 && c <= 0x2FF) ||
         (c >= 0x370 && c <= 0x37D) || (c >= 0x37F && c <= 0x1FFF) || (c >= 0x200C && c <= 0x200D) ||
         (c >= 0x2070 && c <= 0x218F) || (c >= 0x2C00 && c <= 0x2FEF) || (c >= 0x3001 && c <= 0xD7FF) ||
         (c >= 0xF900 && c <= 0xFDCF) || (c >= 0xFDF0 && c <= 0xFFFD) || (c >= 0x10000 && c <= 0xEFFFF))
    { 
      result.append( c);
      found = true;
      
      // NameChar
      c = stream.read();
      while( (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') ||
             (c == '_') || (c == ':') || (c == '-') || (c == '.') || (c == 0xB7) ||
             (c >= 0x300 && c <= 0x36F) || (c >= 0x203F && c <=0x2040) || 
             (c >= 0xC0 && c <= 0xD6) || (c >= 0xD8 && c <= 0xF6) || (c >= 0xF8 && c <= 0x2FF) ||
             (c >= 0x370 && c <= 0x37D) || (c >= 0x37F && c <= 0x1FFF) || (c >= 0x200C && c <= 0x200D) ||
             (c >= 0x2070 && c <= 0x218F) || (c >= 0x2C00 && c <= 0x2FEF) || (c >= 0x3001 && c <= 0xD7FF) ||
             (c >= 0xF900 && c <= 0xFDCF) || (c >= 0xFDF0 && c <= 0xFFFD) || (c >= 0x10000 && c <= 0xEFFFF))
      { 
        result.append( c);
        c = stream.read();
      }
    }
    
    // return unused character
    stream.rewind();
    
    return found;
  }
  
  public final static boolean readNCName( Stream stream, StringBuilder result) throws IOException
  {
    boolean found = false;
    
    // NameStartChar (no colon)
    int c = stream.read();
    if ( (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_') ||  
         (c >= 0xC0 && c <= 0xD6) || (c >= 0xD8 && c <= 0xF6) || (c >= 0xF8 && c <= 0x2FF) ||
         (c >= 0x370 && c <= 0x37D) || (c >= 0x37F && c <= 0x1FFF) || (c >= 0x200C && c <= 0x200D) ||
         (c >= 0x2070 && c <= 0x218F) || (c >= 0x2C00 && c <= 0x2FEF) || (c >= 0x3001 && c <= 0xD7FF) ||
         (c >= 0xF900 && c <= 0xFDCF) || (c >= 0xFDF0 && c <= 0xFFFD) || (c >= 0x10000 && c <= 0xEFFFF))
    { 
      result.append( c);
      found = true;
      
      // NameChar (no colon)
      c = stream.read();
      while( (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') ||
             (c == '_') || (c == '-') || (c == '.') || (c == 0xB7) ||
             (c >= 0x300 && c <= 0x36F) || (c >= 0x203F && c <=0x2040) || 
             (c >= 0xC0 && c <= 0xD6) || (c >= 0xD8 && c <= 0xF6) || (c >= 0xF8 && c <= 0x2FF) ||
             (c >= 0x370 && c <= 0x37D) || (c >= 0x37F && c <= 0x1FFF) || (c >= 0x200C && c <= 0x200D) ||
             (c >= 0x2070 && c <= 0x218F) || (c >= 0x2C00 && c <= 0x2FEF) || (c >= 0x3001 && c <= 0xD7FF) ||
             (c >= 0xF900 && c <= 0xFDCF) || (c >= 0xFDF0 && c <= 0xFFFD) || (c >= 0x10000 && c <= 0xEFFFF))
      { 
        result.append( c);
        c = stream.read();
      }
    }
    
    // return unused character
    stream.rewind();
    
    return found;
  }
  
  public final static boolean readNMToken( Stream stream, StringBuilder result) throws IOException
  {
    boolean found = false;
    
    // NameChar
    int c = stream.read();
    while( (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') ||
           (c == '_') || (c == ':') || (c == '-') || (c == '.') || (c == 0xB7) ||
           (c >= 0x300 && c <= 0x36F) || (c >= 0x203F && c <=0x2040) || 
           (c >= 0xC0 && c <= 0xD6) || (c >= 0xD8 && c <= 0xF6) || (c >= 0xF8 && c <= 0x2FF) ||
           (c >= 0x370 && c <= 0x37D) || (c >= 0x37F && c <= 0x1FFF) || (c >= 0x200C && c <= 0x200D) ||
           (c >= 0x2070 && c <= 0x218F) || (c >= 0x2C00 && c <= 0x2FEF) || (c >= 0x3001 && c <= 0xD7FF) ||
           (c >= 0xF900 && c <= 0xFDCF) || (c >= 0xFDF0 && c <= 0xFFFD) || (c >= 0x10000 && c <= 0xEFFFF))
    { 
      result.append( c);
      c = stream.read();
      found = true;
    }
    
    // return unused character
    stream.rewind();
    
    return found;
  }
  
  public final static boolean read( Stream stream, char[] chars) throws IOException
  {
    for( int i=0; i<chars.length; i++)
    {
      int c = stream.read();
      if ( c != chars[ i])
      {
        stream.rewind();
        return false;
      }
    }
    return true;
  }
  
  private final static char[] xmlnsChars = "xmlns".toCharArray();
}
