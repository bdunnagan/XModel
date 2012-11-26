package org.xmodel.xaction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.xmodel.INode;
import org.xmodel.xml.XmlIO;

public class Main
{
  /**
   * Run the script in the specified file.
   * @param file The file.
   */
  public static void run( String file) throws Exception
  {
    INode root = new XmlIO().read( new FileInputStream( new File( file)));
    XActionDocument doc = new XActionDocument( root);
    IXAction script = doc.createScript( root);
    script.run();
  }
  
  public static void main( String[] args) throws Exception
  {
    if ( args.length == 0)
    {
      String dir = new File( System.getProperty( "user.dir")).getAbsolutePath();
      BufferedReader reader = new BufferedReader( new InputStreamReader( System.in));
      while( true)
      {
        System.out.printf( "%s> ", dir);
        String command = reader.readLine().trim();
        run( command);
      }
    }
    else
    {
      run( args[ 0]);
    }
  }
}
