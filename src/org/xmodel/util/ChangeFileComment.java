/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ChangeFileComment.java
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
package org.xmodel.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeFileComment
{
  public static void main( String[] args) throws Exception
  {
    File path = new File( "/Applications/eclipse342/svn/trunk/client/XModel");
    String header = getHeader(); 
    
    ChangeFileComment cfc = new ChangeFileComment( path);
    cfc.substitute( header);
    
    // dump line count
    int count = 0;
    for( File file: cfc.getAllFiles())
    {
      String content = loadFile( file);
      String[] lines = content.split( "\\n+");
      for( int i=0; i<lines.length; i++)
      {
        lines[ i] = lines[ i].trim();
        if ( lines[ i].length() == 0) continue;
        if ( lines[ i].length() > 2 && lines[ i].charAt( 0) == '/' && lines[ i].charAt( 1) == '/') continue;
        count++;
      }
      System.out.printf( "%s -> %d\n", file, lines.length);
    }
    
    System.out.printf( "Total: "+count);
  }
  
  public ChangeFileComment( File path)
  {
    this.path = path;
  }
  
  private void substitute( String header)
  {
    Pattern pattern = Pattern.compile( "\\A(/\\*.+?\\*/\\s*).*\\Z", Pattern.MULTILINE | Pattern.DOTALL);
    List<File> files = getAllFiles();
    for ( int i=0; i<files.size(); i++)
    {
      File file = (File)files.get( i);
      String content = loadFile( file);
      if ( content != null)
      {
        String comment = substituteFileName( header, file.getName());
        Matcher matcher = pattern.matcher( content);
        if ( matcher.matches())
        {
          int start = matcher.start( 1);
          int end = matcher.end( 1);
          StringBuffer result = new StringBuffer();
          if ( start > 0) result.append( content.substring( 0, start));
          result.append( comment);
          result.append( content.substring( end));
          saveFile( file, result.toString());
        }
        else
        {
          saveFile( file, comment+content);
        }
      }
    }
  }
  
  private static String loadFile( File file)
  {
    try
    {
      StringBuffer result = new StringBuffer();
      FileReader reader = new FileReader( file);
      char[] buffer = new char[ 8192];
      while( reader.ready())
      {
        int nread = reader.read( buffer);
        result.append( buffer, 0, nread);
      }
      reader.close();
      return result.toString();
    }
    catch( IOException e)
    {
      e.printStackTrace( System.err);
      return null;
    }
  }
  
  private static void saveFile( File file, String content)
  {
    try
    {
      System.out.println( file.getName());
      FileWriter writer = new FileWriter( file);
      writer.write( content);
      writer.close();
    }
    catch( IOException e)
    {
      e.printStackTrace( System.err);
    }
  }
  
  private List<File> getAllFiles()
  {
    List<File> result = new ArrayList<File>();
    Stack<File> stack = new Stack<File>();
    stack.push( path);
    while( !stack.empty())
    {
      File dir = (File)stack.pop();
      File[] files = dir.listFiles();
      if ( files != null)      
        for ( int i=0; i<files.length; i++)
        {
          if ( files[ i].isDirectory()) stack.push( files[ i]);
          if ( files[ i].toString().endsWith( ".java")) result.add( files[ i]);
        }
    }
    return result;
  }

  /**
   * Loads and returns the header.
   * @return Returns the header.
   */
  private static String getHeader() throws Exception
  {
    InputStream stream = ChangeFileComment.class.getResourceAsStream( "header.txt");
    BufferedReader reader = new BufferedReader( new InputStreamReader( stream));
    
    StringBuilder sb = new StringBuilder();
    sb.append( "/*"); sb.append( "\n");
    while( reader.ready())
    {
      String line = reader.readLine();
      sb.append( " * ");
      sb.append( line);
      sb.append( "\n");
    }
    sb.append( " */"); sb.append( "\n");
    
    reader.close();
    stream.close();
    
    return sb.toString();
  }
  
  /**
   * Substitute the name of the specified file into the header.
   * @param header The header.
   * @param file The file.
   * @return Returns the tailored header.
   */
  private static String substituteFileName( String header, String file)
  {
    return header.replaceFirst( "[$]FILE", file);
  }
  
  File path;
}
