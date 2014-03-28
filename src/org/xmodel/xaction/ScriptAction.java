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
import java.util.List;
import java.util.Set;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.log.SLog;
import org.xmodel.xaction.debug.Debugger;
import org.xmodel.xpath.expression.IContext;
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

    // special handling of <if>, <elseif>, <else>
    IfAction ifAction = null;
    
    // create script operations
    List<IXAction> list = new ArrayList<IXAction>();
    for( IModelObject element: document.getRoot().getChildren())
    {
      if ( !ignore.contains( element.getType()))
      {
        IXAction action = document.getAction( element);
        if ( action != null) 
        {
          if ( ifAction != null)
          {
            if ( action instanceof ElseAction) ((ElseAction)action).setIf( ifAction);
            else if ( action instanceof ElseifAction) ((ElseifAction)action).setIf( ifAction);
            ifAction = null;
          }
          
          if ( action instanceof IfAction) ifAction = (IfAction)action;
          
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
      try
      {
        debugger.push( context, this);
        for( int i=0; i<actions.length; i++)
        {
          Object[] result = debugger.run( context, actions[ i]);
          if ( result != null) return result;
        }
      }
      catch( RuntimeException e)
      {
        SLog.errorf( this, "Caught next exception at %s ...", getScriptLocationString( getDocument()));
        throw e;
      }
      finally
      {
        debugger.pop( context);
      }
      
      return null;
    }
  }
  
  /**
   * @return Returns a string that describes the location of this script.
   */
  private String getScriptLocationString( XActionDocument document)
  {
    StringBuilder sb = new StringBuilder();
    IModelObject root = null;
    
    while ( document != null)
    {
      root = document.getRoot();
      if ( root == null) continue;
      
      if ( sb.length() > 0) sb.append( " <- ");
      
      String name = Xlate.get( root, "name", (String)null);
      if ( name != null) 
      {
        sb.append( name);
      }
      else
      {
        sb.append( root.getType());
        if ( root.getParent() != null)
        {
          sb.append( '[');
          sb.append( root.getParent().getChildren( root.getType()).indexOf( root) + 1);
          sb.append( ']');
        }
      }
      
      document = document.getParentDocument();
    }
    
    return sb.toString();
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
  private Set<String> ignore;
  private IXAction[] actions;
}
