/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * TryAction.java
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
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.log.Log;
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
    tryScript = document.createScript( "catch", "finally");
    
    // finally script
    IModelObject finallyNode = document.getRoot().getFirstChild( "finally");
    if ( finallyNode != null) finallyScript = document.createScript( finallyNode);
    
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
        log.exception( e);
      }
      
      catchBlock.script = document.createScript( catchElement);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    Object[] result = null;
    
    try
    {
      return tryScript.run( context);
    }
    catch( XActionException e)
    {
      Throwable t = e.getCause();
      CatchBlock catchBlock = findCatchBlock( t);
      if ( catchBlock != null)
      {
        setExceptionVariable( context, t);
        return catchBlock.script.run( context);
      }
    }
    catch( Throwable t)
    {
      CatchBlock catchBlock = findCatchBlock( t);
      if ( catchBlock != null)
      {
        setExceptionVariable( context, t);
        return catchBlock.script.run( context);
      }
    }
    finally
    {
      if ( finallyScript != null) 
        result = finallyScript.run( context);
    }
    
    return result;
  }
  
  /**
   * Find the matching CatchBlock for the specified throwable.
   * @param t The object that was thrown.
   * @return Returns null or the matching CatchBlock.
   */
  private CatchBlock findCatchBlock( Throwable t)
  {
    for( CatchBlock catchBlock: catchBlocks)
      if ( catchBlock.thrownClass.isAssignableFrom( t.getClass()))
        return catchBlock;
    
    return null;
  }
  
  /**
   * Set the exception variable in the specified context scope.
   * @param context The context.
   * @param t The object that was thrown.
   */
  private void setExceptionVariable( IContext context, Throwable t)
  {
    IVariableScope scope = context.getScope();
    if ( scope != null)
    {
      IModelObject exception = XActionException.createExceptionFragment( t, null);
      scope.set( "exception", exception);
    }
  }

  private class CatchBlock
  {
    Class<Throwable> thrownClass;
    ScriptAction script;
  }
  
  private static Log log = Log.getLog( "org.xmodel.xaction");
  
  private final IExpression catchActionExpr = XPath.createExpression(
    "*[ name() = 'catch']");
  
  private ScriptAction tryScript;
  private List<CatchBlock> catchBlocks;
  private ScriptAction finallyScript;
}
