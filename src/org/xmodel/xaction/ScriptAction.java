/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xaction;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * A toplevel action which executes all of its children until one of them fails.
 */
public class ScriptAction extends GuardedAction
{
  protected ScriptAction()
  {
  }
  
  /**
   * Create a ScriptAction which will execute the script at the specified URL.
   * @param loader The class loader.
   * @param url The URL pointing to the script.
   */
  public ScriptAction( ClassLoader loader, URL url) throws XmlException
  {
    this( loader, (new XmlIO()).read( url));
  }
  
  /**
   * Create a ScriptAction which will execute the children of the specified element.
   * @param loader The class loader.
   * @param element The parent element.
   */
  public ScriptAction( ClassLoader loader, IModelObject element)
  {
    if ( element == null) return;
    
    XActionDocument document = new XActionDocument( loader);
    document.setRoot( element);
    setDocument( document);
    
    actions = document.getActions( actionExpr.query( document.getRoot(), null));
    configure( document);
  }
  
  /**
   * Create a script which will execute the specified elements. All of the elements
   * must have the same parent node, which will be used as the root of the action
   * document. The list must contain at least one element.
   * @param loader The class loader.
   * @parma document The document.
   * @param elements The elements to be executed.
   */
  public ScriptAction( XActionDocument document, List<IModelObject> elements)
  {
    if ( elements.size() == 0) return;
    
    actions = new ArrayList<IXAction>( elements.size());
    for( IModelObject element: elements)
    {
      if ( element.isType( "condition") || element.isType( "when") || element.isType( "package")) continue;
      IXAction action = document.getAction( element);
      if ( action != null)
      {
        action.configure( document.getDocument( element));
        actions.add( action);
      }
    }
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
    if ( actions == null) actions = document.getActions( actionExpr.query( document.getRoot(), null));
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

  private final static IExpression actionExpr = XPath.createExpression(
    "*[ not( matches( name(), '^condition|when|package|factory|matcher$'))]");
  
  private List<IXAction> actions;
}
