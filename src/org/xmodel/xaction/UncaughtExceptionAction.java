package org.xmodel.xaction;

import java.lang.Thread.UncaughtExceptionHandler;

import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class UncaughtExceptionAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    classExpr = document.getExpression( "class", true);
    script = document.createScript( "class");
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    if ( script != null)
    {
      try
      {
        String className = classExpr.evaluateString( context);
        Class<?> clss = Class.forName( className);
        UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler( new Handler( handler, clss, script, context));
      }
      catch( ClassNotFoundException e)
      {
        throw new XActionException( e);
      }
    }
    return null;
  }
  
  private static class Handler implements UncaughtExceptionHandler
  {
    public Handler( UncaughtExceptionHandler nextHandler, Class<?> clss, IXAction script, IContext context)
    {
      this.clss = clss;
      this.script = script;
      this.context = context;
      this.nextHandler = nextHandler;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException( Thread t, Throwable e)
    {
      if ( clss.isAssignableFrom( e.getClass()))
      {
        try { script.run( context);} catch( Throwable any) {}
      }
      
      if ( nextHandler != null) nextHandler.uncaughtException( t, e);
    }

    private Class<?> clss;
    private IXAction script;
    private IContext context;
    private UncaughtExceptionHandler nextHandler;
  }

  private IXAction script;
  private IExpression classExpr;
}
