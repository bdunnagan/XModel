/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ScriptAction.java
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.log.SLog;
import org.xmodel.xaction.debug.Debugger;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * An action that executes its children.
 */
public class ScriptAction extends GuardedAction
{
  public ScriptAction()
  {
    ignore = new HashSet<String>();
    ignore( defaultIgnore);
  }
  
  /**
   * Create a ScriptAction which will execute the children of the specified element.
   * @param element The root of the script.
   */
  public ScriptAction( IModelObject element)
  {
    this( ScriptAction.class.getClassLoader(), element);
  }
  
  /**
   * Create a ScriptAction which will execute the children of the specified element except those in the ignore array.
   * @param root The root of the script.
   * @param ignore The element names to ignore.
   */
  public ScriptAction( IModelObject root, String... ignore)
  {
    this( ScriptAction.class.getClassLoader(), root, ignore);
  }
  
  /**
   * Create a ScriptAction while will execute the specified elements as a script.
   * @param elements The script elements to be executed in sequence.
   */
  public ScriptAction( List<IModelObject> elements)
  {
    this( ScriptAction.class.getClassLoader(), elements);
  }
  
  /**
   * Create a ScriptAction which will execute the children of the specified element.
   * @param loader The class loader.
   * @param element The root of the script.
   */
  public ScriptAction( ClassLoader loader, IModelObject element)
  {
    this( loader, element, new String[ 0]);
  }

  /**
   * Create a ScriptAction which will execute the children of the specified element except those in the ignore array.
   * @param loader The class loader.
   * @param root The root of the script.
   * @param ignore The element names to ignore.
   */
  public ScriptAction( ClassLoader loader, IModelObject root, String... ignore)
  {
    if ( root != null)
    {
      XActionDocument document = new XActionDocument( loader);
      document.setRoot( root);
      
      this.ignore = new HashSet<String>();
      ignore( defaultIgnore);
      ignore( ignore);

      configure( document);
    }
  }
  
  /**
   * Create a ScriptAction while will execute the specified elements as a script.
   * @param loader The class loader.
   * @param elements The script elements to be executed in sequence.
   */
  public ScriptAction( ClassLoader loader, List<IModelObject> elements)
  {
    ignore = new HashSet<String>();
    ignore( defaultIgnore);
    
    IModelObject root = new ModelObject( "script");
    for( IModelObject element: elements)
      root.addChild( element.cloneTree());
    
    XActionDocument document = new XActionDocument( loader);
    document.setRoot( root);
    
    configure( document);
  }
  
  /**
   * Ignore elements with the specified names when creating actions.
   * @param ignore The list of names to ignore.
   */
  public void ignore( String... ignore)
  {
    for( int i=0; i<ignore.length; i++) 
      this.ignore.add( ignore[ i]);
  }

  /**
   * @return Returns null or the array of input variables.
   */
  public String[] getInVars()
  {
    return inVars;
  }
  
  /**
   * Returns the actions in the script.
   * @return Returns the actions in the script.
   */
  public IXAction[] getActions()
  {
    if ( actions == null) return new IXAction[ 0];
    return actions;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.XAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    // optionally create local variable context
    privateScope = Xlate.get( document.getRoot(), "scope", "public").equals( "private");
    
    // input-only variables
    if ( document.getRoot().isType( "script"))
    {
      String in = Xlate.get( document.getRoot(), "in", (String)null);
      inVars = (in != null)? in.split( "\\s*,\\s*"): null;
    }

    // create script operations
    List<IXAction> list = new ArrayList<IXAction>();
    ListIterator<IModelObject> iterator = document.getRoot().getChildren().listIterator();
    while( iterator.hasNext())
    {
      IModelObject element = iterator.next();
      
      if ( !ignore.contains( element.getType()))
      {
        IXAction action = document.getAction( element);
        if ( action != null) 
        {
          if ( action instanceof CompoundAction)
          {
            CompoundAction compound = (CompoundAction)action;
            compound.configure( document, iterator);
          }
          
          list.add( action);
        }
      }
    }
    
    actions = list.toArray( new IXAction[ 0]);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    if ( privateScope) context = new StatefulContext( context);
    
    if ( !isDebugging())
    {
      for( int i=0; i<actions.length; i++)
      {
        Object[] result = actions[ i].run( context);
        if ( result != null) return result;
      }
      return null;
    }
    else
    {
      Debugger debugger = getDebugger();
      debugger.push( context, this);
      try
      {
        for( int i=0; i<actions.length; i++)
        {
          try
          {
            Object[] result = debugger.run( context, actions[ i]);
            if ( result != null) return result;
          }
          catch( ScriptException e)
          {
            throw e;
          }
          catch( RuntimeException e)
          {
            String location = getScriptLocationString( actions[ i].getDocument());
            SLog.errorf( this, "Caught next exception at:\n%s", location);
            throw new ScriptException( actions[ i], e);
          }
        }
      }
      finally
      {
        debugger.pop( context);
      }
      
      return null;
    }
  }
  
  /**
   * Pass variables to script.
   * @param argExprs The argument expressions.
   * @param context The calling context.
   * @param nested The nested execution context.
   * @param script The script.
   */
  public static void passVariables( List<IExpression> argExprs, IContext context, IContext nested, IXAction script)
  {
    if ( script instanceof ScriptAction)
    {
      String[] inVars = ((ScriptAction)script).getInVars();
      if ( inVars != null)
      {
        for( int i=0; i<argExprs.size(); i++)
        {
          if ( i == inVars.length) break;
          
          IExpression argExpr = argExprs.get( i);
          switch( argExpr.getType( context))
          {
            case NODES:   nested.set( inVars[ i], argExpr.evaluateNodes( context)); break;
            case STRING:  nested.set( inVars[ i], argExpr.evaluateString( context)); break;
            case NUMBER:  nested.set( inVars[ i], argExpr.evaluateNumber( context)); break;
            case BOOLEAN: nested.set( inVars[ i], argExpr.evaluateBoolean( context)); break; 
            default:      break;
          }
        }
      }
    }
  }
  
  /**
   * Pass variables to script.
   * @param args The arguments.
   * @param context The calling context.
   * @param nested The nested execution context.
   * @param script The script.
   */
  @SuppressWarnings("unchecked")
  public static void passVariables( Object[] args, IContext nested, IXAction script)
  {
    if ( script instanceof ScriptAction)
    {
      String[] inVars = ((ScriptAction)script).getInVars();
      if ( inVars != null)
      {
        for( int i=0; i<args.length; i++)
        {
          if ( i == inVars.length) break;
          
          Object arg = args[ i];
          if ( arg != null)
          {
            if ( arg instanceof List)
            {
              nested.set( inVars[ i], (List<IModelObject>)arg);
            }
            else if ( arg instanceof IModelObject)
            {
              nested.set( inVars[ i], (IModelObject)arg);
            }
            else if ( arg instanceof Number)
            {
              nested.set( inVars[ i], (Number)arg);
            }
            else if ( arg instanceof Boolean)
            {
              nested.set( inVars[ i], (Boolean)arg);
            }
            else
            {
              nested.set( inVars[ i], arg.toString());
            }
          }
        }
      }
    }
  }
    
  /**
   * @return Returns a string that describes the location of this script.
   */
  private static String getScriptLocationString( XActionDocument document)
  {
    LinkedHashSet<IModelObject> elements = new LinkedHashSet<IModelObject>();
    
    while ( document != null)
    {
      IModelObject root = document.getRoot();
      if ( root != null) elements.add( root);
      document = document.getParentDocument();
    }
    
    StringBuilder sb = new StringBuilder();

    IModelObject[] array = elements.toArray( new IModelObject[ 0]);
    sb.append( array[ 0].getType());
    if ( array[ 0].getParent() != null)
    {
      sb.append( '[');
      sb.append( array[ 0].getParent().getChildren().indexOf( array[ 0]) + 1);
      sb.append( ']');
      sb.append( '\n');
    }
    
    for( int i=1; i<array.length; i++)
    {
      if ( array[ i].isType( "script"))
      {
        String name = Xlate.get( array[ i], "name", (String)null);
        if ( name != null)
        {
          sb.append( name);
          sb.append( '\n');
        }
      }
      else
      {
        sb.append( '<'); sb.append( array[ i].getType());
        for( String attrName: array[ i].getAttributeNames())
        {
          if ( attrName.length() != 0 && !attrName.equals( "xaction") && !attrName.equals( "xm:compiled"))
          {
            Object attrValue = array[ i].getAttribute( attrName);
            if ( attrValue != null)
            {
              sb.append( ' ');
              sb.append( attrName); 
              sb.append( '='); 
              sb.append( '"');
              sb.append( attrValue);
              sb.append( '"');
            }
          }
        }
        sb.append( ">");
        sb.append( '\n');
      }
    }
    
    return sb.toString();
  }
  
  public class ScriptException extends RuntimeException
  {
    private static final long serialVersionUID = 9000111268511851458L;

    public ScriptException( IXAction xaction, Throwable cause)
    {
      super( cause);
      this.xaction = xaction;
    }
    
    public IXAction getScript()
    {
      return xaction;
    }
    
    private IXAction xaction;
  }
  
  private final static String[] defaultIgnore = {
    "condition",
    "factory",
    "function",
    "matcher",
    "package",
    "when",
  };

  private boolean privateScope;
  private String[] inVars;
  private Set<String> ignore;
  private IXAction[] actions;
}
