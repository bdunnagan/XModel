package org.xmodel.xpath.parser.nu;

import java.io.IOException;
import java.io.Reader;

public final class Stream
{
  /**
   * @return Returns the current line number.
   */
  public int getLine()
  {
    return line;
  }

  /**
   * @return Returns the current column number.
   */
  public int getColumn()
  {
    return column;
  }
  
  /**
   * Read the next character from the stream.
   * @return Returns the last character read.
   */
  public int read() throws IOException
  {
  }

  /**
   * Rewind the stream one character.
   */
  public void rewind() throws IOException
  {
  }
  
  private Reader reader;
  private int line;
  private int column;
  private char[] buffer;
  private int offset;
  private int length;
}
