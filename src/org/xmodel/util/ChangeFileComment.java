/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class ChangeFileComment
{
  public static void main( String[] args) throws Exception
  {
    File path = new File( "/Applications/eclipse341/svn/trunk/client/XMPlugin/src/dunnagan");
    //File path = new File( "/opt/homes/bdunnagan/accurev/checkpoint/cornerstone/client/XModel/src/dunnagan/bob/xmodel");
    String comment = 
      "/*\n"+
      " * XModel (XML Application Data Modeling Framework)\n"+
      " * Author: Bob Dunnagan (bdunnagan@nc.rr.com)\n" +
      " * Copyright Bob Dunnagan 2009. All rights reserved.\n"+
      " */\n";
    
    ChangeFileComment cfc = new ChangeFileComment( path);
    //cfc.substitute( comment);
    
    int count = 0;
    for( File file: cfc.getAllFiles())
    {
      String content = loadFile( file);
      String[] lines = content.split( "\\n+");
      for( int i=0; i<lines.length; i++)
      {
        lines[ i] = lines[ i].trim();
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
  
  private void substitute( String comment)
  {
    int mask = Pattern.MULTILINE | Pattern.DOTALL;
//    Pattern pattern = Pattern.compile( "\\A(/\\*[^/]+\\*/\\s*).*\\Z", mask);
    Pattern pattern = Pattern.compile( "\\A(/\\*.+?\\*/\\s*).*\\Z", mask);
    List<File> files = getAllFiles();
    for ( int i=0; i<files.size(); i++)
    {
      File file = (File)files.get( i);
      String content = loadFile( file);
      if ( content != null)
      {
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

  File path;
}
