package org.xmodel.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileUtil
{
  /**
   * Read the complete content of the specified text file.
   * @param file The file.
   * @return Returns the content.
   */
  public static String readAll( File file) throws IOException
  {
    char[] buffer = new char[ 1 << 16];
    
    StringBuilder content = new StringBuilder();
    BufferedReader reader = new BufferedReader( new FileReader( file));
    while( reader.ready())
    {
      int count = reader.read( buffer, 0, buffer.length);
      if ( count > 0) content.append( buffer, 0, count);
    }
    
    return content.toString();
  }
}
