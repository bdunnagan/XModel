/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.xml.XmlException;
import dunnagan.bob.xmodel.xml.XmlIO;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

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
    
    actions = document.getActions( document.getRoot().getChildren());
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
      IXAction action = document.getAction( element);
      actions.add( action);
      action.configure( document.getDocument( element));
    }
  }

  /**
   * Returns the actions in the script.
   * @return Returns the actions in the script.
   */
  public List<IXAction> getActions()
  {
    return actions;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.XAction#configure(dunnagan.bob.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    if ( actions == null) actions = document.getActions( actionExpr.query( document.getRoot(), null));
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public void doAction( IContext context)
  {
    try
    {
      if ( actions != null)
        for( IXAction action: actions)
          action.run( context);
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err); 
    }
  }

  private final static IExpression actionExpr = XPath.createExpression(
    "*[ not( matches( name(), '^condition|when|package$'))]");
  
  private List<IXAction> actions;
}
