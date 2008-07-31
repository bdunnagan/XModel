package dunnagan.bob.xmodel.xaction;

import java.util.ArrayList;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;
import dunnagan.bob.xmodel.xpath.variable.IVariableScope;

/**
 * An XAction which executes a script and provides an exception handling script which will handle
 * exceptions thrown during processing of the script. Any type of Throwable can be handled. When
 * an exception is caught, the <i>exception</i> variable holds the instance of the exception.
 */
public class TryAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#configure(dunnagan.bob.xmodel.xaction.XActionDocument)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    // try script
    List<IModelObject> tryActions = tryActionExpr.query( document.getRoot(), null);
    tryScript = new ScriptAction( document, tryActions);
    
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
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
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
  
  private final static IExpression tryActionExpr = XPath.createExpression(
    "*[ name() != 'catch']");
  
  private final static IExpression catchActionExpr = XPath.createExpression(
    "*[ name() = 'catch']");
  
  private ScriptAction tryScript;
  private List<CatchBlock> catchBlocks;
}
