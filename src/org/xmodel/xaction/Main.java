package org.xmodel.xaction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmodel.CurrentThreadExecutor;
import org.xmodel.IModelObject;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

public class Main
{
  /**
   * Parse the specified command-line into an argument array.
   * @param command The command-line.
   * @return Returns the argument array.
   */
  public static String[] parseCommand( String command)
  {
    List<String> args = new ArrayList<String>();
    boolean quoting = false;
    boolean escaping = false;
    int index = 0;
    for( int i=0; i<command.length(); i++)
    {
      char c = command.charAt( i);
      if ( !escaping)
      {
        if ( c == '"')
        {
          quoting = !quoting;
          continue;
        }
        else if ( c == '\\')
        {
          escaping = true;
          continue;
        }
        else if ( c == ' ' && !quoting && !escaping)
        {
          if ( (index + 1) < i)
          {
            args.add( command.substring( index, i));
            index = i;
          }
        }
      }
      else
      {
        if ( c == '\\')
        {
          escaping = false;
          continue;
        }
      }
    }
    
    if ( quoting) throw new IllegalArgumentException( "Unterminated quote.");
    if ( escaping) throw new IllegalArgumentException( "Unterminated escape.");
    
    if ( (index + 1) < command.length())
    {
      args.add( command.substring( index));
    }
    
    return args.toArray( new String[ 0]);
  }
  
  /**
   * Configure variables in the specified context from the command-line arguments.
   * Only strings of the form, 'var="..."', are parsed.
   * @param args The arguments.
   * @param context The context.
   */
  public static void variables( String[] args, IContext context)
  {
    Matcher matcher = assignRegex.matcher( "");
    for( String arg: args)
    {
      matcher.reset( arg);
      if ( matcher.find())
      {
        context.set( matcher.group( 1), matcher.group( 2));
      }
    }
  }
  
  /**
   * Run the script in the specified file.
   * @param args The command-line args.
   */
  public static void run( String[] args) throws Exception
  {
    IContext context = new StatefulContext();
    context.setExecutor( new CurrentThreadExecutor());
    
    variables( args, context);
    
    IModelObject root = new XmlIO().read( new FileInputStream( new File( args[ 0])));
    XActionDocument doc = new XActionDocument( root);
    IXAction script = doc.createScript( root);
    
    script.run( context);
    
    CurrentThreadExecutor executor = (CurrentThreadExecutor)context.getExecutor();
    while( true) executor.process();
  }
  
  private static Pattern assignRegex = Pattern.compile( "(\\w++)\\s*+=\\s*+[\"]([^\"]++)[\"]");
  
  public static void main( String[] args) throws Exception
  {
    if ( args.length == 0)
    {
      String dir = new File( System.getProperty( "user.dir")).getAbsolutePath();
      BufferedReader reader = new BufferedReader( new InputStreamReader( System.in));
      while( true)
      {
        System.out.printf( "%s> ", dir);
        args = parseCommand( reader.readLine().trim());
        if ( args[ 0].equals( "exit")) break;
        run( args);
      }
    }
    else
    {
      run( args);
    }
  }
}
