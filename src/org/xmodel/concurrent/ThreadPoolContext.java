package org.xmodel.concurrent;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * An implementation of IContext that uses a ThreadPoolDispatcher to dispatch all of its mutative
 * operations, such as setting variables.
 */
public class ThreadPoolContext extends StatefulContext
{
  public ThreadPoolContext( ThreadPoolDispatcher dispatcher)
  {
    super();
    this.dispatcher = dispatcher;
  }
    
  /**
   * Write-lock the model of this context for updating.
   * @deprecated Use getModel().writeLock instead.
   */
  public void lock() throws InterruptedException
  {
    dispatcher.model.writeLock( 15, TimeUnit.MINUTES);
  }
  
  /**
   * Write-unlock the model of this context.
   * @deprecated Use getModel().writeUnlock instead.
   */
  public void unlock()
  {
    dispatcher.model.writeUnlock();
  }
  
  /**
   * Write-lock the model of this context for updating.
   */
  private void writeLock() throws InterruptedException
  {
    dispatcher.model.writeLock( 15, TimeUnit.MINUTES);
  }
  
  /**
   * Write-unlock the model of this context.
   */
  private void writeUnlock()
  {
    dispatcher.model.writeUnlock();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.StatefulContext#set(java.lang.String, java.lang.String)
   */
  @Override
  public String set( String name, String value)
  {
    try { writeLock();} catch( InterruptedException e) { throw new IllegalStateException();}
    try
    {
      return super.set( name, value);
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.StatefulContext#set(java.lang.String, java.lang.Number)
   */
  @Override
  public Number set( String name, Number value)
  {
    try { writeLock();} catch( InterruptedException e) { throw new IllegalStateException();}
    try
    {
      return super.set( name, value);
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.StatefulContext#set(java.lang.String, java.lang.Boolean)
   */
  @Override
  public Boolean set( String name, Boolean value)
  {
    try { writeLock();} catch( InterruptedException e) { throw new IllegalStateException();}
    try
    {
      return super.set( name, value);
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.StatefulContext#set(java.lang.String, java.util.List)
   */
  @Override
  public List<IModelObject> set( String name, List<IModelObject> value)
  {
    try { writeLock();} catch( InterruptedException e) { throw new IllegalStateException();}
    try
    {
      return super.set( name, value);
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.StatefulContext#set(java.lang.String, org.xmodel.IModelObject)
   */
  @Override
  public List<IModelObject> set( String name, IModelObject value)
  {
    try { writeLock();} catch( InterruptedException e) { throw new IllegalStateException();}
    try
    {
      return super.set( name, value);
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.StatefulContext#define(java.lang.String, org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public void define( String name, IExpression expression)
  {
    try { writeLock();} catch( InterruptedException e) { throw new IllegalStateException();}
    try
    {
      super.define( name, expression);
    }
    finally
    {
      writeUnlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.StatefulContext#getModel()
   */
  @Override
  public IModel getModel()
  {
    return dispatcher.model;
  }

  private ThreadPoolDispatcher dispatcher;
}
