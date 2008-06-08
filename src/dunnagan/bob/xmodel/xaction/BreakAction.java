/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import dunnagan.bob.xmodel.*;
import dunnagan.bob.xmodel.net.IDispatcher;
import dunnagan.bob.xmodel.net.ManualDispatcher;
import dunnagan.bob.xmodel.net.ModelServer;
import dunnagan.bob.xmodel.xml.XmlException;
import dunnagan.bob.xmodel.xml.XmlIO;
import dunnagan.bob.xmodel.xml.IXmlIO.Style;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;
import dunnagan.bob.xmodel.xpath.expression.IExpression.ResultType;
import dunnagan.bob.xmodel.xpath.variable.IVariableScope;

/**
 * A GuardedAction which pauses execution and attempts to read the standard input device.
 * This action is intended for debugging purposes. If a parsable integer is entered, then
 * the breakpoint will be skipped that number of times. Entering -1 will disable the 
 * breakpoint for the duration. If the input cannot be parsed as an integer, but it can
 * be parsed as an XPath expression, then the XPath becomes the guard condition of the 
 * action.
 */
public class BreakAction extends GuardedAction
{
  public BreakAction()
  {
    state = State.stepOver;
    
    xmlIO = new XmlIO();
    xmlIO.skipOutputPrefix( "break");
    xmlIO.setOutputStyle( Style.debug);
    
    if ( separator == null)
    {
      separator = new char[ 80];
      Arrays.fill( separator, '_');
    }
  }
  
  /**
   * Returns the BreakAction currently registered for the calling thread or null.
   * @return Returns the BreakAction currently registered for the calling thread or null.
   */
  public static BreakAction getThreadBreakAction()
  {
    if ( threadBreakStacks == null) threadBreakStacks = new ThreadLocal<Stack<BreakAction>>();
    Stack<BreakAction> stack = threadBreakStacks.get();
    if ( stack != null && !stack.empty()) return stack.peek();
    return null;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#configure(dunnagan.bob.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    // configure
    history = new ArrayList<String>( 1);
    reader = new BufferedReader( new InputStreamReader( System.in));
    
    script = document.createScript( actionExpr);;
    
    watches = new ArrayList<IExpression>();
    for( IModelObject watchSpec: document.getRoot().getChildren( "watch")) 
      watches.add( document.getExpression( watchSpec));
    
    
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    ModelServer server = null;
    IModel model = context.getModel();
    try
    {
      // push this breakpoint on the stack
      pushThreadBreak();
      
      // start xmodel server
      try
      {
        modelDispatcher = model.getDispatcher();
        breakDispatcher = new ManualDispatcher();
        model.setDispatcher( breakDispatcher);
        server = new ModelServer( context.getModel(), -1);
        server.setContext( context);
        server.start( port);
      }
      catch( BindException e)
      {
        server = null;
      }
      catch( IOException e)
      {
        e.printStackTrace( System.err);
        server = null;
      }
      
      // read past input
      try { while( System.in.available() != 0) System.in.read();} catch( Exception e) {}
      
      // show header
      System.out.println( "");
      System.out.println( separator);
      System.out.println( prefix);
      
      // execute script if any
      if ( script.getActions().size() > 0)
      {
        showElement( prefix, getDocument().getRoot(), maxLines);
        System.out.println( prefix);
        script.run( context);
        prompt( context, null);
      }
      else
      {
        // show parent of breakpoint
        IModelObject parent = getDocument().getRoot().getParent();
        if ( parent != null)
        {
          showElement( prefix, getDocument().getRoot().getParent(), maxLines);
          System.out.println( prefix);
        }
        
        // prompt
        System.out.println( separator);
        System.out.println( prefix);
        prompt( context, document.getRoot());
      }
    }
    finally
    {
      // pop this breakpoint off the stack
      popThreadBreak();
      
      // shutdown server
      model.setDispatcher( modelDispatcher);
      breakDispatcher.process();
      if ( server != null) server.stop();
    }
  }
  
  /**
   * Called by XAction when the specified action is started and before <code>prompt</code> is called.
   * @param action The action to be executed.
   * @return Returns true if action should prompt before executing.
   */
  protected boolean startAction( IXAction action)
  {
    // hacked and tested to stability
    switch( state)
    {
      case stepIn:
      {
        current = action;
        return true;
      }
      
      case stepOver:
      {
        if ( current == null)
        {
          current = action;
          return true;
        }
        return false;
      }
      
      case stepOut:
      {
        stepOutDepth++;
        return false;
      }
    }
    
    return true;
  }
  
  /**
   * Called by XAction when the specified action has completed.
   * @param action
   */
  protected void endAction( IXAction action)
  {
    // hacked and tested to stability
    switch( state)
    {
      case stepIn:
      {
        current = null;
      }
      break;
      
      case stepOver:
      {
        if ( action == current) current = null;
      }
      break;
      
      case stepOut:
      {
        if ( stepOutDepth-- < 0) state = State.stepOver;
        if ( action == current) current = null;
      }
      break;
    }
  }
  
  /**
   * Called by XAction to prompt the user and handle input before the action is executed.
   * @param context The execution context.
   * @param location The step location.
   */
  protected void prompt( IContext context, IModelObject location)
  {
    if ( condition == null || condition.evaluateBoolean( context))
    {
      System.out.println( separator);
      System.out.println( prefix);
      
      // show/set location
      this.location = location;
      if ( location != null) showElement( prefix, location, maxLines);
      
      // show watch list
      if ( watches.size() > 0)
      {
        System.out.println( prefix);
        for( IExpression watch: watches)
          showText( prefix+watch.toString()+"=", watch.evaluateString( context), 1);
        System.out.println( prefix);
      }
      
      // loop until recognized input
      while( true)
      {
        try
        {
          // flush stderr
          System.err.flush();
          
          // prompt
          System.out.println( separator);
          System.out.println( prefix);
          System.out.printf( "-> ");
          
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
          e.printStackTrace( System.err);
        }
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
      breakDispatcher.process();
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
      state = State.stepOver;
      return true;
    }
    
    // parse >
    if ( input.equals( ">"))
    {
      state = State.stepIn;
      return true;
    }
    
    // parse <
    if ( input.equals( "<"))
    {
      state = State.stepOut;
      stepOutDepth = 0;
      return true;
    }
    
    // parse $
    if ( input.equals( "$"))
    {
      showVariables( context);
      return false;
    }
    
    // parse |
    if ( input.charAt( 0) == '|')
    {
      runAction( context, input.substring( 1).trim());
      return false;
    }
    
    // parse ~
    if ( input.charAt( 0) == '~')
    {
      showStack( prefix);
      return false;
    }
    
    // parse @ by itself
    if ( input.equals( "@"))
    {
      IModelObject branch = ModelAlgorithms.cloneBranch( location);
      branch = branch.getRoot();
      showElement( prefix, branch, maxLines);
      return false;
    }
    
    // parse !
    if ( input.charAt( 0) == '!')
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
      ResultType type = expression.getType( context);
      if ( expression != null) 
      {
        if ( type != ResultType.BOOLEAN)
        {
          showResult( expression, context);
        }
        else
        {
          condition = expression;
          showText( prefix, "Breakpoint condition: "+condition, maxLines);
          return true;
        }
      }
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
    System.out.printf( "%s  [Return] to step over the next action.\n", prefix);
    System.out.printf( "%s  < to step out of the current action.\n", prefix);
    System.out.printf( "%s  > to step into the next action.\n", prefix);
    System.out.printf( "%s  An expression to be evaluated in the current context.\n", prefix);
    System.out.printf( "%s  $ to print a summary of all context defined variables.\n", prefix);
    System.out.printf( "%s  @ to reprint the current breakpoint location.\n", prefix);
    System.out.printf( "%s  ~ will dump a stack trace to the console.\n", prefix);
    System.out.printf( "%s  | followed by a file path to execute an XAction from a file.\n", prefix);
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
    for( String name: scope.getAll())
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
      String xml = xmlIO.write( ModelAlgorithms.cloneTree( element, factory));
      int indent = getAncestorCount( element);
      for( int i=0; i<indent; i++) prefix += "  ";
      showText( prefix, xml, maxLines);
    }
    catch( Exception e)
    {
    }
  }

  /**
   * Returns the count of ancestors.
   * @param element The element.
   * @return Returns the count of ancestors.
   */
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
      for( int i=0; i<maxLines; i++)
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
   * Run the XAction defined in the specified file.
   * @parma context The execution context.
   * @param file The file.
   */
  private void runAction( IContext context, String file)
  {
    try
    {
      IModelObject element = xmlIO.read( new FileInputStream( file));
      if ( element != null) runAction( context, element);
    }
    catch( IOException e)
    {
      System.out.printf( "%sFile not found.", prefix);
    }
    catch( XmlException e)
    {
      System.out.printf( "%sInvalid xml document.", prefix);
    }
  }
  
  /**
   * Run the XAction defined in the specified element.
   * @parma context The execution context.
   * @param element The element.
   */
  private void runAction( IContext context, IModelObject element)
  {
    // create new root document
    XActionDocument document = getDocument().getDocument( element);
    
    // get action
    IXAction action = document.getAction( element);
    if ( action != null) action.run( context);
  }
  
  /**
   * Push this BreakAction onto the thread-local break stack.
   */
  private void pushThreadBreak()
  {
    Stack<BreakAction> stack = threadBreakStacks.get();
    if ( stack == null)
    {
      stack = new Stack<BreakAction>();
      threadBreakStacks.set( stack);
    }
    stack.push( this);
  }
  
  /**
   * Pop this BreakAction of of the thread-local break stack.
   */
  private void popThreadBreak()
  {
    Stack<BreakAction> stack = threadBreakStacks.get();
    stack.pop();
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
  
  private final static IExpression actionExpr = XPath.createExpression(
    "*[ not( matches( name(), 'when|condition|skip|lines|watch'))]");
  
  private static char[] separator;
  private static String prefix = "    ";
  private static int maxLines = 1000;
  private static int port = 17311;
  
  private XmlIO xmlIO;
  private IDispatcher modelDispatcher;
  private ManualDispatcher breakDispatcher;
  private BufferedReader reader;
  private IExpression condition;
  private ScriptAction script;
  private List<IExpression> watches;
  private List<String> history;
  private IModelObject location;
  
  private enum State { stepIn, stepOver, stepOut};
  private State state;
  private IXAction current;
  private int stepOutDepth;
  
  private static ThreadLocal<Stack<BreakAction>> threadBreakStacks;
}
