/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * BreakAction.java
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
package org.xmodel.xaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.ModelObjectFactory;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;
import org.xmodel.xaction.debug.Debugger;
import org.xmodel.xaction.debug.IBreakHandler;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpression.ResultType;
import org.xmodel.xpath.expression.StatefulContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * A GuardedAction which pauses execution and attempts to read the standard input device.
 * This action is intended for debugging purposes. If a parsable integer is entered, then
 * the breakpoint will be skipped that number of times. Entering -1 will disable the 
 * breakpoint for the duration. If the input cannot be parsed as an integer, but it can
 * be parsed as an XPath expression, then the XPath becomes the guard condition of the 
 * action.
 */
public class BreakAction extends GuardedAction implements IBreakHandler
{
  public BreakAction()
  {
    xmlIO = new XmlIO();
    xmlIO.skipOutputPrefix( "break");
    xmlIO.setOutputStyle( Style.printable);
    
    history = new ArrayList<String>( 1);
    reader = new BufferedReader( new InputStreamReader( System.in));
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IBreakHandler#breakpoint(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void breakpoint( IContext context)
  {
    // read past input
    try { while( System.in.available() != 0) System.in.read();} catch( Exception e) {}

    // get current location
    Debugger debugger = getDebugger();
    IXAction frame = debugger.getCurrentFrame( debugger.getDebugThreadID());
    IModelObject location = (frame != null)? frame.getDocument().getRoot(): new ModelObject( "(none)");
    
    // prompt
    StatefulContext breakContext = new StatefulContext( context);
    breakContext.set( "@", location);
    prompt( breakContext, location);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IBreakHandler#resume()
   */
  @Override
  public void resume()
  {
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    Debugger debugger = getDebugger();
    if ( debugger == null)
    {
      SLog.warnf( this, "Not in debug mode at, %s", this);
      return null;
    }

    // TODO: IBreakHandler should be factored out of BreakAction
    debugger.setBreakHandler( this);
    debugger.breakpoint( context);
    
    return null;
  }
  
  /**
   * Called by XAction to prompt the user and handle input before the action is executed.
   * @param context The execution context.
   * @param location The step location.
   */
  protected void prompt( IContext context, IModelObject location)
  {
    // loop until recognized input
    while( true)
    {
      try
      {
        // flush stderr
        System.err.flush();
        
        // prompt
        IModelObject locationLine = location.cloneObject();
        locationLine.removeAttribute( "xaction");
        locationLine.removeAttribute( "xm:compiled");
        System.out.println( ModelAlgorithms.createIdentityPath( location, true));
        System.out.println( "  "+XmlIO.write( Style.printable, locationLine));
        System.out.printf( "[%s]> ", Thread.currentThread().getName());
        
        // get input
        StringBuilder builder = new StringBuilder();
        while( true)
        {
          String line = readLine();
          line = line.trim();
          if ( line.length() > 1 && line.charAt( line.length() - 1) == '+')
          {
            builder.append( line);
            builder.deleteCharAt( builder.length() - 1);
            System.out.print( prefix+"+> ");
          }
          else
          {
            builder.append( line);
            break;
          }
        }
        
        String input = builder.toString();
        if ( input.length() > 0) history.add( input);
        if ( process( context, input)) break;
      }
      catch( IOException e)
      {
        log.exception( e);
      }
    }
  }
  
  /**
   * Read a line from the standard input and perform ManualDispatcher processing if necessary.
   * @return Returns a line of text from the standard input.
   */
  private String readLine() throws IOException
  {
    while( true)
    {
      if ( reader.ready()) return reader.readLine();
      try { Thread.sleep( 50);} catch( Exception e) {}
    }
  }
  
  /**
   * Process a debug command. 
   * @param context The context.
   * @param input The command.
   * @return Returns true if execution should continue.
   */
  private boolean process( IContext context, String input)
  {
    // parse ?
    if ( input.equals( "?"))
    {
      showUsage();
      return false;
    }

    // parse empty
    if ( input.length() == 0)
    {
      Debugger debugger = getDebugger();
      debugger.stepOver( debugger.getDebugThreadID());
      return true;
    }
    
    else if ( input.equals( ">>"))
    {
      Debugger debugger = getDebugger();
      debugger.resume( debugger.getDebugThreadID());
      return true;
    }
    
    // parse >
    else if ( input.equals( ">"))
    {
      Debugger debugger = getDebugger();
      debugger.stepIn( debugger.getDebugThreadID());
      return true;
    }
    
    // parse <
    else if ( input.equals( "<"))
    {
      Debugger debugger = getDebugger();
      debugger.stepOut( debugger.getDebugThreadID());
      return true;
    }
    
    // parse $
    else if ( input.equals( "$"))
    {
      showVariables( context);
      return false;
    }
    
    // parse ~
    else if ( input.charAt( 0) == '~')
    {
      showStack( prefix);
      return false;
    }
    
    // parse # by itself
    else if ( input.equals( "#"))
    {
      showContext( context);
      return false;
    }
    
    // parse !
    else if ( input.charAt( 0) == '!')
    {
      history.remove( history.size() - 1);
      String pattern = input.substring( 1);
      if ( pattern.length() == 0)
      {
        for( int i=0; i<history.size(); i++)
          System.out.printf( "%s[%d] %s\n", prefix, (i+1), history.get( i));
        System.out.println( prefix);
        return false;
      }
      else
      {
        int index = parseNumber( pattern) - 1;
        if ( index != Integer.MAX_VALUE)
        {
          if ( index >= 0 && index < history.size())
          {
            String command = history.get( index);
            System.out.printf( "%s!%s\n", prefix, command);
            return process( context, command);
          }
          return false;
        }
        else
        {
          for( String command: history)
            if ( command.startsWith( pattern))
            {
              System.out.printf( "%s!%s\n", prefix, command);
              return process( context, command);
            }
        }
      }
    }
    
    // try to parse xpath
    try
    {
      IExpression expression = XPath.compileExpression( input);
      if ( expression != null) showResult( expression, context);
      return false;
    }
    catch( Exception e)
    {
      System.out.printf( "%sSyntax error: %s\n\n", prefix, e.getMessage());
      return false;
    }
    catch( Error e)
    {
      System.out.printf( "%sSyntax error: %s\n\n", prefix, e.getMessage());
      return false;
    }
  }
  
  /**
   * Returns Integer.MAX_VALUE or the parsed number.
   * @param text The text to be parsed.
   * @return Returns Integer.MAX_VALUE or the parsed number.
   */
  private int parseNumber( String text)
  {
    try
    {
      return Integer.parseInt( text);
    }
    catch( Exception e)
    {
      return Integer.MAX_VALUE;
    }
  }
  
  /**
   * Show the usage summary.
   */
  private void showUsage()
  {
    System.out.println( prefix);
    System.out.printf( "%sEnter one of the following:\n", prefix);
    System.out.printf( "%s  ? to repeat this message.\n", prefix);
    System.out.printf( "%s  [RETURN] to step over the next action.\n", prefix);
    System.out.printf( "%s  < to step out of the current action.\n", prefix);
    System.out.printf( "%s  > to step into the next action.\n", prefix);
    System.out.printf( "%s  >> to resume.\n", prefix);
    System.out.printf( "%s  An expression to be evaluated in the current context.\n", prefix);
    System.out.printf( "%s  $ to print a summary of all context defined variables.\n", prefix);
    System.out.printf( "%s  # to dump the context stack.\n", prefix);
    System.out.printf( "%s  ~ will dump a stack trace to the console.\n", prefix);
    System.out.printf( "%s  !, by itself, to show history.\n", prefix);
    System.out.printf( "%s  ! followed by the index of the command to execute.\n", prefix);
    System.out.printf( "%s  ! followed by the first few letters of the command to execute.\n", prefix);
    System.out.println( prefix);
  }
  
  /**
   * Show the variables defined on the specified context.
   * @param context The context.
   */
  private void showVariables( IContext context)
  {
    System.out.println( prefix);
    
    IVariableScope scope = context.getScope();
    Collection<String> variables = scope.getAll();
    String[] array = variables.toArray( new String[ 0]);
    Arrays.sort( array);
    
    for( String name: array)
    {
      System.out.print( prefix);
      System.out.println( name);
    }
    
    System.out.println( prefix);
  }
  
  /**
   * Show the result of evaluating the specified expression in the specified context.
   * @param expression The expression.
   * @param context The context.
   */
  private void showResult( IExpression expression, IContext context)
  {
    System.out.println( prefix);
    
    if ( expression.getType( context) == ResultType.NODES)
    {
      List<IModelObject> nodes = expression.evaluateNodes( context);
      for( int i=0; i<nodes.size(); i++)
      {
        showElement( prefix+"["+i+"] ", nodes.get( i), maxLines);
        System.out.println( prefix);
      }
    }
    else
    {
      String result = expression.evaluateString( context);
      showText( prefix, result, maxLines);
      System.out.println( prefix);
    }
  }
  
  /**
   * Printout the specified element.
   * @param prefix The per-line prefix.
   * @param element The element.
   * @param maxLines The maximum number of lines to print.
   */
  private void showElement( String prefix, IModelObject element, int maxLines)
  {
    try
    {
      IModelObject clone = ModelAlgorithms.cloneTree( element, factory);
      String xml = xmlIO.write( clone);
      showText( prefix, xml, maxLines);
    }
    catch( Exception e)
    {
    }
  }
  
  /**
   * Count the lines in the specified text.
   * @param text The text.
   * @return Returns the number of lines.
   */
  @SuppressWarnings("unused")
  private int lineCount( String text)
  {
    int count = 0;
    int index = text.indexOf( "\n");
    while( index >= 0)
    {
      count++;
      index = text.indexOf( "\n", index+1);
    }
    return count;
  }
  
  /**
   * Returns the count of ancestors.
   * @param element The element.
   * @return Returns the count of ancestors.
   */
  @SuppressWarnings("unused")
  private int getAncestorCount( IModelObject element)
  {
    int count = 0;
    element = element.getParent();
    while( element != null)
    {
      count++;
      element = element.getParent();
    }
    return count;
  }
  
  /**
   * Show text with each line preceded by the specified prefix.
   * @param prefix The per-line prefix.
   * @param text The text.
   * @param maxLines The maximum number of lines to print.
   */
  private void showText( String prefix, String text, int maxLines)
  {
    try
    {
      String[] lines = text.split( "\n");
      if ( maxLines <= 0) maxLines = lines.length;
      for( int i=0; i<maxLines && i<lines.length; i++)
        System.out.printf( "%s%s\n", prefix, lines[ i]);
    }
    catch( Exception e)
    {
    }
  }
  
  /**
   * Show a stack trace with the specified prefix.
   * @param prefix The prefix.
   */
  private void showStack( String prefix)
  {
    StringBuilder builder = new StringBuilder();
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    for( int i=0; i<trace.length; i++)
    {
      builder.append( i); builder.append( ". ");
      builder.append( trace[ i].toString());
      builder.append( '\n');
    }
    showText( prefix, builder.toString(), 64);
  }
  
  /**
   * Show the context stack.
   * @param context The context.
   */
  private void showContext( IContext context)
  {
    List<IContext> stack = new ArrayList<IContext>();
    while( context != null)
    {
      stack.add( 0, context);
      context = context.getParent();
    }
    
    StringBuilder sb = new StringBuilder();
    for( IContext c: stack)
    {
      sb.append( String.format( "%x", System.identityHashCode( c)));
      sb.append( ": ");
      sb.append( ModelAlgorithms.createIdentityExpression( c.getObject()));
      sb.append( "\n");
    }
    
    showText( prefix, sb.toString(), 64);
  }
  
  /**
   * Factory which removes certain XActionDocument attributes.
   */
  private IModelObjectFactory factory = new ModelObjectFactory() {
    public IModelObject createClone( IModelObject object)
    {
      IModelObject clone = super.createClone( object);
      clone.removeAttribute( "xm:compiled");
      return clone;
    }
  };
  
  private static String prefix = "";
  private static int maxLines = 1000;
  
  private XmlIO xmlIO;
  private BufferedReader reader;
  private List<String> history;
  
  private static Log log = Log.getLog( "org.xmodel.xaction");
}
