package org.xmodel.xpath.parser.nu;

import java.io.IOException;
import java.io.Reader;

public final class Tokenizer
{
  public enum Token
  {
    none,
    whitespace,
    qname,
    end
  }
  
  /**
   * Consume at least the specified number of tokens of the specified kind.
   * @param kind The type of token to consume.
   * @param count The number of tokens to consume.
   * @param result Returns the tokens that were consumed iff the method returns true.
   * @return Returns true if the tokens could be consumed.
   */
  public boolean consumeAtLeast( Token kind, int count, StringBuilder result) throws IOException
  {
    if ( kind == Token.none) readToken();
    
    while( this.kind == kind && count > 0 && reader.ready())
    {
      count--;
      result.append( c);
      readToken();
    }
    
    return count <= 0;
  }
  
  /**
   * Consume exactly the specified number of tokens of the specified kind.
   * @param kind The type of token to consume.
   * @param count The number of tokens to consume.
   * @param result Returns the tokens that were consumed iff the method returns true.
   * @return Returns true if the tokens could be consumed.
   */
  public boolean consumeExactly( Token kind, int count, StringBuilder result) throws IOException
  {
    if ( kind == Token.none) readToken();
    
    while( this.kind == kind && reader.ready())
    {
      count--;
      result.append( c);
      readToken();
    }
    
    return count <= 0;
  }
  
  /**
   * Read the next character.
   */
  private void readToken() throws IOException
  {
    c = reader.read();
    if ( c == -1) { kind = Token.end; return;}
    
    
  }
  
  private Reader reader;
  private Token kind;
  private int c;
}
