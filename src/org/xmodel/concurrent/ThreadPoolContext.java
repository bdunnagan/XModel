package org.xmodel.concurrent;

import java.util.List;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.ModelRegistry;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * An implementation of IContext that uses a ThreadPoolDispatcher to dispatch all of its mutative
 * operations, such as setting variables.  Instances of this class are created via ThreadPoolDispatcher.
 */
public class ThreadPoolContext extends StatefulContext
{
  public ThreadPoolContext( ThreadPoolDispatcher dispatcher)
  {
    super();
    this.dispatcher = dispatcher;
  }
  
  /**
   * Lock the model of this context for updating.
   */
  public void lock() throws InterruptedException
  {
    // lock
    dispatcher.lock();
    
    // configure thread with this model
    ModelRegistry registry = ModelRegistry.getInstance();
    threadPreviousModel = registry.getModel( false);
    registry.setModel( dispatcher.model);
    
    // update this for debugging purposes
    dispatcher.model.setThread( Thread.currentThread());
  }
  
  /**
   * Unock the model of this context.
   */
  public void unlock()
  {
    ModelRegistry registry = ModelRegistry.getInstance();
    registry.setModel( threadPreviousModel);
    
    dispatcher.unlock();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.StatefulContext#set(java.lang.String, java.lang.String)
   */
  @Override
  public String set( String name, String value)
  {
    try { lock();} catch( InterruptedException e) { throw new IllegalStateException();}
    try
    {
      return super.set( name, value);
    }
    finally
    {
      unlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.StatefulContext#set(java.lang.String, java.lang.Number)
   */
  @Override
  public Number set( String name, Number value)
  {
    try { lock();} catch( InterruptedException e) { throw new IllegalStateException();}
    try
    {
      return super.set( name, value);
    }
    finally
    {
      unlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.StatefulContext#set(java.lang.String, java.lang.Boolean)
   */
  @Override
  public Boolean set( String name, Boolean value)
  {
    try { lock();} catch( InterruptedException e) { throw new IllegalStateException();}
    try
    {
      return super.set( name, value);
    }
    finally
    {
      unlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.StatefulContext#set(java.lang.String, java.util.List)
   */
  @Override
  public List<IModelObject> set( String name, List<IModelObject> value)
  {
    try { lock();} catch( InterruptedException e) { throw new IllegalStateException();}
    try
    {
      return super.set( name, value);
    }
    finally
    {
      unlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.StatefulContext#set(java.lang.String, org.xmodel.IModelObject)
   */
  @Override
  public List<IModelObject> set( String name, IModelObject value)
  {
    try { lock();} catch( InterruptedException e) { throw new IllegalStateException();}
    try
    {
      return super.set( name, value);
    }
    finally
    {
      unlock();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.StatefulContext#define(java.lang.String, org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public void define( String name, IExpression expression)
  {
    try { lock();} catch( InterruptedException e) { throw new IllegalStateException();}
    try
    {
      super.define( name, expression);
    }
    finally
    {
      unlock();
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
  private IModel threadPreviousModel;
}
