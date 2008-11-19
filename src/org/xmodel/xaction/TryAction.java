/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.variable.IVariableScope;


/**
 * An XAction which executes a script and provides an exception handling script which will handle
 * exceptions thrown during processing of the script. Any type of Throwable can be handled. When
 * an exception is caught, the <i>exception</i> variable holds the instance of the exception.
 */
public class TryAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    // try script
    tryScript = document.createScript( "catch");
    
    // catch blocks
    List<IModelObject> catchElements = catchActionExpr.query( document.getRoot(), null);
    catchBlocks = new ArrayList<CatchBlock>( catchElements.size());
    for( int i=0; i<catchElements.size(); i++)
    {
      IModelObject catchElement = catchElements.get( i);
      CatchBlock catchBlock = new CatchBlock();
      
      String className = Xlate.get( catchElement, "class", "java.lang.Throwable");
      try
      {
        ClassLoader loader = document.getClassLoader();
        catchBlock.thrownClass = (Class<Throwable>)loader.loadClass( className);
        catchBlocks.add( catchBlock);
      }
      catch( Exception e)
      {
        e.printStackTrace( System.err);
      }
      
      catchBlock.script = new ScriptAction( document.getClassLoader(), catchElement);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    try
    {
      tryScript.run( context);
    }
    catch( XActionException e)
    {
      Throwable t = e.getCause();
      for( CatchBlock catchBlock: catchBlocks)
      {
        if ( catchBlock.thrownClass.isAssignableFrom( t.getClass()))
        {
          IVariableScope scope = context.getScope();
          if ( scope != null)
          {
            IModelObject exception = XActionException.createExceptionFragment( t, null);
            scope.set( "exception", exception);
          }
          catchBlock.script.run( context);
        }
      }
    }
    catch( Throwable t)
    {
      for( CatchBlock catchBlock: catchBlocks)
      {
        if ( catchBlock.thrownClass.isAssignableFrom( t.getClass()))
        {
          IVariableScope scope = context.getScope();
          if ( scope != null)
          {
            IModelObject exception = XActionException.createExceptionFragment( t, null);
            scope.set( "exception", exception);
          }
          catchBlock.script.run( context);
        }
      }
    }
  }

  private class CatchBlock
  {
    Class<Throwable> thrownClass;
    ScriptAction script;
  }
  
  private final static IExpression catchActionExpr = XPath.createExpression(
    "*[ name() = 'catch']");
  
  private ScriptAction tryScript;
  private List<CatchBlock> catchBlocks;
}
