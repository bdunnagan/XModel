/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import java.lang.reflect.Constructor;

import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * An XAction which throws a Java runtime exception and thus terminates XAction processing.
 */
public class ThrowAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#configure(dunnagan.bob.xmodel.xaction.XActionDocument)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    // load throwable class
    String className = Xlate.get( document.getRoot(), "class", "");
    try
    {
      ClassLoader loader = document.getClassLoader();
      clss = (Class<RuntimeException>)loader.loadClass( className);
    }
    catch( ClassNotFoundException e)
    {
    }
    
    try
    {
      if ( clss == null) clss = (Class<RuntimeException>)ThrowAction.class.getClassLoader().loadClass( className);
    }
    catch( ClassNotFoundException e)
    {
    }
    
    if ( clss == null) clss = RuntimeException.class;
    
    // get optional message
    messageExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    RuntimeException exception = null;

    try
    {
      if ( messageExpr != null)
      {
        String message = messageExpr.evaluateString( context);
        Constructor<RuntimeException> constructor = clss.getConstructor( String.class);
        exception = constructor.newInstance( message);
      }
      else
      {
        exception = clss.newInstance();
      }
    }
    catch( Exception e)
    {
      exception = new XActionException( "Unable to create instance of clss: "+clss, e);
    }
    
    throw exception; 
  }

  private Class<RuntimeException> clss;
  private IExpression messageExpr;
}
