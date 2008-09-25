/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;


/**
 * A toplevel action which executes all of its children until one of them fails.
 */
public class ScriptAction extends GuardedAction
{
  public ScriptAction()
  {
    ignore = new HashSet<String>();
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
  public List<IXAction> getActions()
  {
    if ( actions == null) return Collections.emptyList();
    return actions;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.XAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    actions = new ArrayList<IXAction>();
    for( IModelObject element: document.getRoot().getChildren())
    {
      if ( !ignore.contains( element.getType()))
      {
        IXAction action = document.getAction( element);
        if ( action != null) actions.add( action);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void doAction( IContext context)
  {
    if ( actions != null)
      for( IXAction action: actions)
        action.run( context);
  }

  private final static String[] defaultIgnore = {
    "condition",
    "factory",
    "matcher",
    "package",
    "when",
  };
  
  private Set<String> ignore;
  private List<IXAction> actions;
}
