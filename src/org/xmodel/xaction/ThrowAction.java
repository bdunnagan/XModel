/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import java.lang.reflect.Constructor;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * An XAction which throws a Java runtime exception and thus terminates XAction processing.
 */
public class ThrowAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
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
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
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
