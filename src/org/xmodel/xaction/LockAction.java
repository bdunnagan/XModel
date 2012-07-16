package org.xmodel.xaction;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class LockAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    readExpr = document.getExpression( "read", true);
    writeExpr = document.getExpression( "write", true);
    
    script = document.createScript();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    //
    // Acquire read locks
    //
    for( IModelObject element: readExpr.evaluateNodes( context))
    {
      ReadWriteLock newLock = new ReentrantReadWriteLock();
      ReadWriteLock actualLock = locks.putIfAbsent( element, newLock);
      if ( actualLock == null) actualLock = newLock;
      actualLock.readLock().lock();
    }
    
    //
    // Acquire write locks
    //
    for( IModelObject element: writeExpr.evaluateNodes( context))
    {
      ReadWriteLock newLock = new ReentrantReadWriteLock();
      ReadWriteLock actualLock = locks.putIfAbsent( element, newLock);
      if ( actualLock == null) actualLock = newLock;
      actualLock.writeLock();
    }
    
    //
    // Execute body
    //
    Object[] result = script.run( context);
    
    //
    // Release write locks
    //
    for( IModelObject element: writeExpr.evaluateNodes( context))
    {
      ReadWriteLock lock = locks.remove( element);
      lock.writeLock().unlock();
    }
    
    //
    // Release read locks
    //
    for( IModelObject element: readExpr.evaluateNodes( context))
    {
      ReadWriteLock lock = locks.remove( element);
      lock.readLock().unlock();
    }
    
    return result;
  }
  
  private static ConcurrentHashMap<IModelObject, ReadWriteLock> locks = new ConcurrentHashMap<IModelObject, ReadWriteLock>();
  
  private IExpression readExpr;
  private IExpression writeExpr;
  private ScriptAction script;
}
